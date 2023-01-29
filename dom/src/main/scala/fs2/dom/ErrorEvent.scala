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

abstract class ErrorEvent[F[_]] private[dom] extends Event[F] {}

object ErrorEvent {
  def apply[F[_]](event: dom.ErrorEvent)(implicit F: Sync[F]): ErrorEvent[F] =
    new WrappedErrorEvent(event)
}

private final class WrappedErrorEvent[F[_]](val event: dom.ErrorEvent)(implicit
    val F: Sync[F]
) extends ErrorEventImpl[F]

private trait ErrorEventImpl[F[_]] extends ErrorEvent[F] with EventImpl[F] {
  def event: dom.ErrorEvent
  implicit def F: Sync[F]
}
