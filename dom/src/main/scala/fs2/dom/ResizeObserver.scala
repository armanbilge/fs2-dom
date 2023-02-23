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

import scalajs.js
import org.scalajs.dom
import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.effect.kernel.Resource

trait ResizeObserver[F[_]] {

  def observe(target: dom.Element): F[Unit]

  def observe(target: dom.Element, options: dom.ResizeObserverOptions): F[Unit]

  def unobserve(target: dom.Element): F[Unit]

  def disconnect(): F[Unit]

}

object ResizeObserver {

  def apply[F[_]](
      callback: (js.Array[dom.ResizeObserverEntry], dom.ResizeObserver) => F[Unit]
  )(implicit F: Async[F]): Resource[F, ResizeObserver[F]] = for {
    dispatcher <- Dispatcher.parallel[F]
    jsObserver <- Resource.make(
      F.delay(new dom.ResizeObserver((a, b) => dispatcher.unsafeRunAndForget(callback(a, b))))
    )(obs => F.delay(obs.disconnect()))
  } yield new ResizeObserver[F] {

    override def observe(target: dom.Element, options: dom.ResizeObserverOptions): F[Unit] =
      F.delay(jsObserver.observe(target, options))

    override def observe(target: dom.Element): F[Unit] =
      F.delay(jsObserver.observe(target))

    override def unobserve(target: dom.Element): F[Unit] =
      F.delay(jsObserver.unobserve(target))

    override def disconnect(): F[Unit] = F.delay(jsObserver.disconnect())

  }

}
