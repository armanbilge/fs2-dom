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

abstract class PopStateEvent[F[_]] private[dom] extends Event[F] {

  def state[S: Serializer]: Either[Throwable, S]

}

object PopStateEvent {
  def apply[F[_]](event: dom.PopStateEvent)(implicit F: Sync[F]): PopStateEvent[F] =
    new WrappedPopStateEvent(event)
}

private final class WrappedPopStateEvent[F[_]](val event: dom.PopStateEvent)(implicit
    val F: Sync[F]
) extends PopStateEventImpl[F]

private trait PopStateEventImpl[F[_]] extends PopStateEvent[F] with EventImpl[F] {
  def event: dom.PopStateEvent
  implicit def F: Sync[F]

  def state[S](implicit serializer: Serializer[S]) = serializer.deserialize(event.state)
}
