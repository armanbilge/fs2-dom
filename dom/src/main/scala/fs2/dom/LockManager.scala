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
import cats.effect.kernel.Poll
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.effect.syntax.all._
import cats.syntax.all._
import fs2.dom.facade.LockRequestOptions
import org.scalajs.dom

abstract class LockManager[F[_]] private {

  def exclusive(name: String): Resource[F, Unit]

  def tryExclusive(name: String): Resource[F, Boolean]

  def shared(name: String): Resource[F, Unit]

  def tryShared(name: String): Resource[F, Boolean]

}

object LockManager {

  private[dom] def apply[F[_]](manager: facade.LockManager)(implicit F: Async[F]): LockManager[F] =
    new LockManager[F] {

      def exclusive(name: String) = request(name, "exclusive", false).void

      def tryExclusive(name: String) = request(name, "exclusive", true).map(_.isDefined)

      def shared(name: String) = request(name, "shared", false).void

      def tryShared(name: String) = request(name, "shared", true).map(_.isDefined)

      def request(name: String, _mode: String, _ifAvailable: Boolean) =
        for {
          dispatcher <- Dispatcher.sequential

          abort <-
            if (_ifAvailable)
              Resource.pure[F, Option[dom.AbortController]](None)
            else
              Resource.eval(F.delay(Some(new dom.AbortController)))

          startGate <- Resource.eval(F.deferred[facade.Lock])
          endGate <- Resource.eval(F.deferred[Unit])

          request <- F.background {
            F.fromPromise {
              F.delay {
                val options = new LockRequestOptions {
                  mode = _mode
                  ifAvailable = _ifAvailable
                }
                abort.foreach(ctrl => options.signal = ctrl.signal)

                manager.request(
                  name,
                  options,
                  lock => dispatcher.unsafeToPromise(startGate.complete(lock) *> endGate.get)
                )
              }
            }
          }

          lock <- Resource.makeFull { (poll: Poll[F]) =>
            poll(startGate.get).onCancel(abort.foldMapA(ctrl => F.delay(ctrl.abort())))
          }(_ => endGate.complete(()) *> request.flatMap(_.embedNever))

        } yield Option(lock)
    }

}
