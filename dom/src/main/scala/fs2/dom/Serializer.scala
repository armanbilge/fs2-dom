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

import scala.scalajs.js

/** @see [[https://developer.mozilla.org/en-US/docs/Glossary/Serializable_object]]
  *
  * @todo So far the only instance of [[Serializer]] is for [[Unit]].
  * Track progress in [[https://github.com/armanbilge/fs2-dom/issues/59 fs2-dom#59]]).
  */
sealed abstract class Serializer[A] private {

  def serialize(a: A): js.Any

  def deserialize(serialized: js.Any): Either[Throwable, A]

}

object Serializer {

  implicit def unit: Serializer[Unit] = new Serializer[Unit] {
    def serialize(u: Unit): js.Any = u
    def deserialize(serialized: js.Any): Either[Throwable, Unit] = Either.unit
  }

  // used in test
  private[dom] implicit def int: Serializer[Int] = new Serializer[Int] {
    def serialize(i: Int): js.Any = i
    def deserialize(serialized: js.Any): Either[Throwable, Int] = (serialized: Any) match {
      case i: Int => Right(i)
      case x      => Left(new NumberFormatException(x.toString))
    }
  }

}
