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

import cats.data.State
import cats.effect.kernel.Ref
import cats.effect.kernel.Sync
import cats.effect.std.MapRef
import cats.syntax.all._
import org.scalajs.dom.Storage

private[dom] object StorageMapRef {

  def apply[F[_]](storage: Storage)(implicit F: Sync[F]): MapRef[F, String, Option[String]] =
    key =>
      new Ref[F, Option[String]] {

        def get: F[Option[String]] = F.delay(Option(storage.getItem(key)))

        def set(a: Option[String]): F[Unit] = a.fold(F.delay(storage.removeItem(key))) { data =>
          F.delay(storage.setItem(key, data))
        }

        def access: F[(Option[String], Option[String] => F[Boolean])] = F.delay {
          val snapshot = storage.getItem(key)
          val setter = (a: Option[String]) =>
            F.delay {
              if (storage.getItem(key) eq snapshot) {
                a match {
                  case None        => storage.removeItem(key)
                  case Some(value) => storage.setItem(key, value)
                }
                true
              } else false
            }
          (Option(snapshot), setter)
        }

        def tryUpdate(f: Option[String] => Option[String]): F[Boolean] =
          update(f).as(true)

        def tryModify[B](f: Option[String] => (Option[String], B)): F[Option[B]] =
          modify(f).map(Some(_))

        def update(f: Option[String] => Option[String]): F[Unit] =
          F.delay {
            f(Option(storage.getItem(key))) match {
              case None        => storage.removeItem(key)
              case Some(value) => storage.setItem(key, value)
            }
          }

        def modify[B](f: Option[String] => (Option[String], B)): F[B] =
          F.delay {
            val (newValue, b) = f(Option(storage.getItem(key)))
            newValue match {
              case (None)      => storage.removeItem(key)
              case Some(value) => storage.setItem(key, value)
            }
            b
          }

        def tryModifyState[B](state: State[Option[String], B]): F[Option[B]] =
          tryModify(state.run(_).value)

        def modifyState[B](state: State[Option[String], B]): F[B] =
          modify(state.run(_).value)
      }

}
