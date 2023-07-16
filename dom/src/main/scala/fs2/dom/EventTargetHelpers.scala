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
import org.scalajs.dom

private[dom] object EventTargetHelpers {

  def listen[F[_], E <: dom.Event](target: dom.EventTarget, `type`: String)(implicit
      F: Async[F]
  ): Stream[F, E] = Stream.repeatEval(listenOnce(target, `type`))

  def listenOnce[F[_], E <: dom.Event](target: dom.EventTarget, `type`: String)(implicit
      F: Async[F]
  ): F[E] =
    F.async[E] { cb =>
      F.delay {
        val ctrl = new dom.AbortController
        target.addEventListener[E](
          `type`,
          (e: E) => cb(Right(e)),
          new dom.EventListenerOptions {
            once = true
            signal = ctrl.signal
          }
        )
        Some(F.delay(ctrl.abort()))
      }
    }
}
