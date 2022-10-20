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
import cats.effect.std.MapRef
import fs2.concurrent.Signal
import org.scalajs.dom

sealed trait Storage[F[_]] extends MapRef[F, String, Option[String]] {

  def events: Stream[F, Storage.Event]

  def length: Signal[F, Int]

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

      def events =
        fs2.dom.events[F, dom.StorageEvent](dom.window, "storage").map(Event.fromStorageEvent(_))

      def length = new Signal[F, Int] {

        def get = F.delay(storage.length)

        def discrete = Stream.eval(get) ++ events.evalMap(_ => get)

        def continuous = Stream.repeatEval(get)

      }

      def key(i: Int) = F.delay(Option(storage.key(i)))

      def clear = F.delay(storage.clear())

      def apply(key: String) = new WrappedRef[F, Option[String]] {
        def unsafeGet(): Option[String] = Option(storage.getItem(key))
        def unsafeSet(a: Option[String]): Unit = a match {
          case None        => storage.removeItem(key)
          case Some(value) => storage.setItem(key, value)
        }
      }

    }
}
