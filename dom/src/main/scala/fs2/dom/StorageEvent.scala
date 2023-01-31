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

package fs2.dom

import cats.effect.kernel.Async
import org.scalajs.dom

abstract class StorageEvent[F[_]] private[dom] extends Event[F] {

  def key: Option[String]

  def newValue: Option[String]

  def oldValue: Option[String]

  def storageArea: Storage[F]

  def url: String

}

object StorageEvent {
  def apply[F[_]](event: dom.StorageEvent)(implicit F: Async[F]): StorageEvent[F] =
    new WrappedStorageEvent(event)
}

private final class WrappedStorageEvent[F[_]](val event: dom.StorageEvent)(implicit
    val F: Async[F]
) extends StorageEventImpl[F]

private trait StorageEventImpl[F[_]] extends StorageEvent[F] with EventImpl[F] {
  def event: dom.StorageEvent
  implicit def F: Async[F]

  def key = Option(event.key)
  def newValue = Option(event.newValue)
  def oldValue = Option(event.oldValue)
  def storageArea = Storage(event.storageArea)
  def url: String = event.url
}
