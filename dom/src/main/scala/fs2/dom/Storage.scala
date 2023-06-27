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

import cats.syntax.all._
import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class Storage[F[_]] private {

  def events(window: Window[F]): Stream[F, Storage.Event]

  def length: F[Int]

  def getItem(key: String): F[Option[String]]

  def setItem(key: String, item: String): F[Unit]

  def removeItem(key: String): F[Unit]

  def key(i: Int): F[Option[String]]

  def clear: F[Unit]

}

object Storage {

  sealed abstract class Event {
    def url: String
  }

  object Event {
    final case class Cleared(url: String) extends Event
    final case class Added(key: String, value: String, url: String) extends Event
    final case class Removed(key: String, value: String, url: String) extends Event
    final case class Updated(key: String, oldValue: String, newValue: String, url: String)
        extends Event

    private[Storage] def fromStorageEvent[F[_]](ev: StorageEvent[F]): Event =
      ev.key.fold[Event](Cleared(ev.url)) { key =>
        (ev.oldValue, ev.newValue) match {
          case (Some(oldValue), None)           => Removed(key, oldValue, ev.url)
          case (None, Some(newValue))           => Added(key, newValue, ev.url)
          case (Some(oldValue), Some(newValue)) => Updated(key, oldValue, newValue, ev.url)
          case (None, None)                     => throw new AssertionError
        }
      }
  }

  private[dom] def apply[F[_]: Sync](storage: dom.Storage): Storage[F] =
    new WrappedStorage(storage)

  private final case class WrappedStorage[F[_]](storage: dom.Storage)(implicit F: Sync[F])
      extends Storage[F] {

    def events(window: Window[F]) =
      window.storageEvents.mapFilter { ev =>
        Option.when(ev.storageArea == this)(Event.fromStorageEvent(ev))
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
