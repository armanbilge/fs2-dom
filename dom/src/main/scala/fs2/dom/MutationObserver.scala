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

import cats.syntax.all._
import org.scalajs.dom
import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync

abstract class MutationObserver[F[_]] private[dom] {

  def observe(target: Node[F], options: dom.MutationObserverInit): F[Unit]

  def disconnect: F[Unit]

  def takeRecords: F[List[dom.MutationRecord]]

}

object MutationObserver {

  def apply[F[_]](
      callback: (List[dom.MutationRecord], MutationObserver[F]) => F[Unit]
  )(implicit F: Async[F]): Resource[F, MutationObserver[F]] = for {
    dispatcher <- Dispatcher.parallel[F]
    jsObserver <- Resource.make(
      F.delay(
        new dom.MutationObserver((a, b) =>
          dispatcher.unsafeRunAndForget(callback(a.toList, fromJsObserver(b)))
        )
      )
    )(obs => F.delay(obs.disconnect()))
  } yield fromJsObserver(jsObserver)

  private def fromJsObserver[F[_]](
      jsObserver: dom.MutationObserver
  )(implicit F: Sync[F]): MutationObserver[F] =
    new MutationObserver[F] {

      def observe(target: Node[F], options: dom.MutationObserverInit): F[Unit] =
        F.delay(jsObserver.observe(target.asInstanceOf[dom.Node], options))

      def disconnect: F[Unit] = F.delay(jsObserver.disconnect())

      def takeRecords: F[List[dom.MutationRecord]] =
        F.delay(jsObserver.takeRecords()).map(_.toList)

    }

}
