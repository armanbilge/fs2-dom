/*
 * Copyright 2022 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fs2
package dom

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import fs2.concurrent.Channel
import fs2.concurrent.Signal
import org.scalajs.dom

sealed abstract class Storage[F[_]] {

  def events: Stream[F, Storage.Event]

  def length: Signal[F, Int]

  def getItem(key: String): F[Option[String]]

  def setItem(key: String, item: String): F[Unit]

  def removeItem(key: String): F[Unit]

  def key(i: Int): F[Option[String]]

  def clear: F[Unit]

}

object Storage {

  def local[F[_]: Async]: Storage[F] = apply(dom.window.localStorage)

  def session[F[_]: Async]: Storage[F] = apply(dom.window.sessionStorage)

  sealed abstract class Event {
    def url: String
  }

  object Event {
    final case class Cleared(url: String) extends Event
    final case class Added(key: String, value: String, url: String) extends Event
    final case class Removed(key: String, value: String, url: String) extends Event
    final case class Updated(key: String, oldValue: String, newValue: String, url: String)
        extends Event

    private[Storage] def fromStorageEvent(ev: dom.StorageEvent): Event =
      Option(ev.key).fold[Event](Cleared(ev.url)) { key =>
        (Option(ev.oldValue), Option(ev.newValue)) match {
          case (Some(oldValue), None)           => Removed(key, oldValue, ev.url)
          case (None, Some(newValue))           => Added(key, newValue, ev.url)
          case (Some(oldValue), Some(newValue)) => Updated(key, oldValue, newValue, ev.url)
          case (None, None)                     => throw new AssertionError
        }
      }
  }

  private[dom] def apply[F[_]](storage: dom.Storage)(implicit F: Async[F]): Storage[F] =
    new Storage[F] {

      def events = {
        val setup = for {
          dispatcher <- Dispatcher.sequential[F]
          abort <- AbortController[F]
          ch <- Resource.eval {
            Channel.unbounded[F, dom.StorageEvent].flatTap { ch =>
              F.delay {
                dom.window.addEventListener[dom.StorageEvent](
                  "storage",
                  ev => dispatcher.unsafeRunAndForget(ch.send(ev)),
                  new dom.EventListenerOptions {
                    signal = abort
                  }
                )
              }
            }
          }
        } yield ch

        Stream.resource(setup).flatMap(_.stream).mapFilter { ev =>
          Option.when(ev.storageArea eq storage)(Event.fromStorageEvent(ev))
        }
      }

      def length = new Signal[F, Int] {

        def get = F.delay(storage.length)

        def discrete = Stream.eval(get) ++ events.evalMap(_ => get)

        def continuous = Stream.repeatEval(get)

      }

      def getItem(key: String) =
        F.delay(Option(storage.getItem(key)))

      def setItem(key: String, item: String) =
        F.delay(storage.setItem(key, item))

      def removeItem(key: String) =
        F.delay(storage.removeItem(key))

      def key(i: Int) = F.delay(Option(storage.key(i)))

      def clear = F.delay(storage.clear())

    }
}
