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

abstract class MessageEvent[F[_]] private[dom] extends Event[F] {}

object MessageEvent {
  def apply[F[_]](event: dom.MessageEvent)(implicit F: Sync[F]): MessageEvent[F] =
    new WrappedMessageEvent(event)
}

private final class WrappedMessageEvent[F[_]](val event: dom.MessageEvent)(implicit
    val F: Sync[F]
) extends MessageEventImpl[F]

private trait MessageEventImpl[F[_]] extends MessageEvent[F] with EventImpl[F] {
  def event: dom.MessageEvent
  implicit def F: Sync[F]
}
