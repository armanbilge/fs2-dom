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
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.syntax.all._
import fs2.concurrent.Channel
import org.scalajs.dom.Event
import org.scalajs.dom.EventListenerOptions
import org.scalajs.dom.EventTarget

private[dom] trait EventTargetDsl[F[_]] {
  def listen[E <: Event](target: EventTarget, `type`: String): Resource[F, Stream[F, E]]
}

private[dom] object EventTargetDsl {

  implicit def interpreter[F[_] : Async, E <: Event]: EventTargetDsl[F] = new EventTargetDsl[F] {
    override def listen[E <: Event](target: EventTarget, `type`: String): Resource[F,Stream[F,E]] = {
      val setup = for {
        dispatcher <- Dispatcher.sequential[F]
        abort <- AbortDsl[F].abortSignal
        ch <- Resource.eval {
          Channel.unbounded[F, E].flatTap { ch =>
            Sync[F].delay {
              target.addEventListener[E](
                `type`,
                (ev: E) => dispatcher.unsafeRunAndForget(ch.send(ev)),
                new EventListenerOptions {
                  signal = abort
                }
              )
            }
          }
        }
      } yield ch

      setup.map(_.stream)
    }
  }

  def apply[F[_]](implicit ev: EventTargetDsl[F]) = ev
}
