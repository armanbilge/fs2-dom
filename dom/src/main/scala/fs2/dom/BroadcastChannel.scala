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
import cats.effect.kernel.Resource
import fs2.Stream
import org.scalajs.dom

abstract class BroadcastChannel[F[_], A] private {
  def name: String

  def postMessage(a: A): F[Unit]

  def messages: Stream[F, MessageEvent[F, A]]
}

object BroadcastChannel {

  def apply[F[_], A](
      channelName: String
  )(implicit F: Async[F], A: Serializer[A]): Resource[F, BroadcastChannel[F, A]] =
    Resource.make(F.delay(new dom.BroadcastChannel(channelName)))(bc => F.delay(bc.close())).map {
      bc =>
        new BroadcastChannel[F, A] {
          def name = channelName
          def postMessage(a: A) = F.delay(bc.postMessage(A.serialize(a)))
          def messages =
            events[F, dom.MessageEvent](bc, "message").evalMap(MessageEvent.deserialize(_))
        }
    }

}
