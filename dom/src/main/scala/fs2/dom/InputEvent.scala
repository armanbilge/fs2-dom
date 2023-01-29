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

abstract class InputEvent[F[_]] private[dom] extends UIEvent[F] {}

object InputEvent {
  def apply[F[_]](event: dom.InputEvent)(implicit F: Sync[F]): InputEvent[F] =
    new WrappedInputEvent(event)
}

private final class WrappedInputEvent[F[_]](val event: dom.InputEvent)(implicit val F: Sync[F])
    extends InputEventImpl[F]

private trait InputEventImpl[F[_]] extends InputEvent[F] with UIEventImpl[F] {
  def event: dom.InputEvent
  implicit def F: Sync[F]
}
