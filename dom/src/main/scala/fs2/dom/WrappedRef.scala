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

import cats.data.State
import cats.effect.kernel.Ref
import cats.effect.kernel.Sync
import cats.syntax.all._

import scala.scalajs.js

private[dom] abstract class WrappedRef[F[_], A](implicit F: Sync[F]) extends Ref[F, A] {

  def unsafeGet(): A

  def unsafeSet(a: A): Unit

  def get: F[A] = F.delay(unsafeGet())

  def set(a: A): F[Unit] = F.delay(unsafeSet(a))

  def access: F[(A, A => F[Boolean])] = F.delay {
    val snapshot = unsafeGet()
    val setter = (a: A) =>
      F.delay {
        if (unsafeGet().asInstanceOf[js.Any] eq snapshot.asInstanceOf[js.Any]) {
          unsafeSet(a)
          true
        } else false
      }
    (snapshot, setter)
  }

  def tryUpdate(f: A => A): F[Boolean] =
    update(f).as(true)

  def tryModify[B](f: A => (A, B)): F[Option[B]] =
    modify(f).map(Some(_))

  def update(f: A => A): F[Unit] =
    F.delay(unsafeSet(f(unsafeGet())))

  def modify[B](f: A => (A, B)): F[B] =
    F.delay {
      val (a, b) = f(unsafeGet())
      unsafeSet(a)
      b
    }

  def tryModifyState[B](state: State[A, B]): F[Option[B]] =
    tryModify(state.run(_).value)

  def modifyState[B](state: State[A, B]): F[B] =
    modify(state.run(_).value)

}
