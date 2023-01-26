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

abstract class PointerEvent[F[_]] private[dom] extends MouseEvent[F] {}

object PointerEvent {
  def apply[F[_]](event: dom.PointerEvent)(implicit F: Sync[F]): PointerEvent[F] =
    new WrappedPointerEvent(event)
}

private final class WrappedPointerEvent[F[_]](val event: dom.PointerEvent)(implicit val F: Sync[F])
    extends PointerEventImpl[F]

private trait PointerEventImpl[F[_]] extends PointerEvent[F] with MouseEventImpl[F] {
  def event: dom.PointerEvent
  implicit def F: Sync[F]
}
