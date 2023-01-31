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

import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class HashChangeEvent[F[_]] private[dom] extends Event[F] {

  def oldURL: String

  def newURL: String

}

object HashChangeEvent {
  def apply[F[_]](event: dom.HashChangeEvent)(implicit F: Sync[F]): HashChangeEvent[F] =
    new WrappedHashChangeEvent(event)
}

private final class WrappedHashChangeEvent[F[_]](val event: dom.HashChangeEvent)(implicit
    val F: Sync[F]
) extends HashChangeEventImpl[F]

private trait HashChangeEventImpl[F[_]] extends HashChangeEvent[F] with EventImpl[F] {
  def event: dom.HashChangeEvent
  implicit def F: Sync[F]

  def oldURL = event.oldURL
  def newURL = event.newURL
}
