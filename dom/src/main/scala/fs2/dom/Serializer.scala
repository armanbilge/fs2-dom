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

import scala.scalajs.js

/** @see [[https://developer.mozilla.org/en-US/docs/Glossary/Serializable_object]]
  *
  * @todo So far the only implicit instance of [[Serializer]] is for `Unit`.
  * Track progress in [[https://github.com/armanbilge/fs2-dom/issues/59 fs2-dom#59]]).
  */
sealed abstract class Serializer[A] private { outer =>

  def serialize(a: A): js.Any

  def deserialize(serialized: js.Any): Either[Throwable, A]

  def imap[B](f: A => B)(g: B => A): Serializer[B] = new Serializer[B] {
    def serialize(b: B) = outer.serialize(g(b))
    def deserialize(serialized: js.Any) = outer.deserialize(serialized).map(f)
  }

  def iemap[B](f: A => Either[Throwable, B])(g: B => A): Serializer[B] = new Serializer[B] {
    def serialize(b: B) = outer.serialize(g(b))
    def deserialize(serialized: js.Any) = outer.deserialize(serialized).flatMap(f)
  }

}

object Serializer {

  val any: Serializer[js.Any] = new Serializer[js.Any] {
    def serialize(a: js.Any) = a
    def deserialize(serialized: js.Any) = Right(serialized)
  }

  implicit def unit: Serializer[Unit] =
    any.iemap { a =>
      Either.cond(
        js.isUndefined(a),
        (),
        new RuntimeException(s"$a is not undefined")
      )
    }(identity(_))

}
