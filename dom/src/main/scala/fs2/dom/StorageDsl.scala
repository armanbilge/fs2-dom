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

trait StorageDsl[F[_]] {

  def events(typ: StorageDsl.Type): Stream[F, StorageDsl.Event]

  def length(typ: StorageDsl.Type): F[Int]

  def getItem(typ: StorageDsl.Type, key: String): F[Option[String]]

  def setItem(typ: StorageDsl.Type, key: String, item: String): F[Unit]

  def removeItem(typ: StorageDsl.Type, key: String): F[Unit]

  def key(typ: StorageDsl.Type, i: Int): F[Option[String]]

  def clear(typ: StorageDsl.Type): F[Unit]
}

object StorageDsl {

  sealed trait Type {
    final def fold[X](onLocal: => X,
                      onSession: => X): X = this match {
      case Type.Local => onLocal
      case Type.Session => onSession
    }

    private[StorageDsl] def toScalaJsDom = fold(dom.window.localStorage, dom.window.sessionStorage)
  }

  object Type {
    case object Local extends Type
    case object Session extends Type
  }

  sealed abstract class Event {
    def url: String
  }

  object Event {
    final case class Cleared private (url: String) extends Event
    final case class Added private (key: String, value: String, url: String) extends Event
    final case class Removed private (key: String, value: String, url: String) extends Event
    final case class Updated private (key: String, oldValue: String, newValue: String, url: String)
        extends Event

    private[StorageDsl] def fromStorageEvent(ev: dom.StorageEvent): Event =
      Option(ev.key).fold[Event](Cleared(ev.url)) { key =>
        (Option(ev.oldValue), Option(ev.newValue)) match {
          case (Some(oldValue), None)           => Removed(key, oldValue, ev.url)
          case (None, Some(newValue))           => Added(key, newValue, ev.url)
          case (Some(oldValue), Some(newValue)) => Updated(key, oldValue, newValue, ev.url)
          case (None, None)                     => throw new AssertionError
        }
      }
  }

  implicit def interpreter[F[_]](implicit F: Async[F]): StorageDsl[F] =
    new StorageDsl[F] {

      def events(typ: Type) =
        fs2.dom.events[F, dom.StorageEvent](dom.window, "storage").mapFilter { ev =>
          if (ev.storageArea eq typ.toScalaJsDom)
            Some(Event.fromStorageEvent(ev))
          else
            None
        }

      def length(typ: Type) = F.delay(typ.toScalaJsDom.length)

      def getItem(typ: Type, key: String) =
        F.delay(Option(typ.toScalaJsDom.getItem(key)))

      def setItem(typ: Type, key: String, item: String) =
        F.delay(typ.toScalaJsDom.setItem(key, item))

      def removeItem(typ: Type, key: String) =
        F.delay(typ.toScalaJsDom.removeItem(key))

      def key(typ: Type, i: Int) = F.delay(Option(typ.toScalaJsDom.key(i)))

      def clear(typ: Type) = F.delay(typ.toScalaJsDom.clear())

    }

  def apply[F[_]](implicit ev: StorageDsl[F]) = ev
}

