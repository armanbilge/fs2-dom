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
import cats.syntax.all._
import org.scalajs.dom

import scala.scalajs.js

abstract class MessageEvent[F[_], A] private[dom] extends Event[F] {

  def data: A

  def origin: String

}

object MessageEvent {
  def apply[F[_]](event: dom.MessageEvent)(implicit F: Sync[F]): MessageEvent[F, js.Any] =
    new WrappedMessageEvent(event, event.data.asInstanceOf[js.Any])

  private[dom] def deserialize[F[_], A](
      event: dom.MessageEvent
  )(implicit F: Sync[F], A: Serializer[A]): F[MessageEvent[F, A]] =
    A.deserialize(event.data.asInstanceOf[js.Any]).liftTo[F].map(new WrappedMessageEvent(event, _))
}

private final class WrappedMessageEvent[F[_], A](
    val event: dom.MessageEvent,
    val data: A
)(implicit val F: Sync[F])
    extends MessageEventImpl[F, A]

private trait MessageEventImpl[F[_], A] extends MessageEvent[F, A] with EventImpl[F] {
  def event: dom.MessageEvent
  implicit def F: Sync[F]

  def origin = event.origin
}
