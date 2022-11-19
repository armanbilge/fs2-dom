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
import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.EventListenerOptions

abstract class Storage[F[_]] private {

  def events: Stream[F, Storage.Event]

  def length: F[Int]

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
    final case class Cleared private (url: String) extends Event
    final case class Added private (key: String, value: String, url: String) extends Event
    final case class Removed private (key: String, value: String, url: String) extends Event
    final case class Updated private (key: String, oldValue: String, newValue: String, url: String)
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
        fs2.dom
          .events[F, dom.StorageEvent](dom.window, "storage")
          .mapFilter { ev =>
            if (ev.storageArea eq storage)
              Some(Event.fromStorageEvent(ev))
            else
              None
          }

      def length = F.delay(storage.length)

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
