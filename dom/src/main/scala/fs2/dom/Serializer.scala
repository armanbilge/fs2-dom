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

import cats.Invariant
import cats.effect.kernel.Sync
import cats.syntax.all._
import io.circe.Decoder
import io.circe.Encoder
import io.circe.scalajs._

import scala.scalajs.js

/** @see [[https://developer.mozilla.org/en-US/docs/Glossary/Serializable_object]]
  */
trait Serializer[F[_], A] { outer =>

  def serialize(a: A): F[js.Any]

  def deserialize(serialized: js.Any): F[A]

  final def imap[B](f: A => B)(g: B => A)(implicit F: Invariant[F]): Serializer[F, B] =
    new Serializer[F, B] {
      def serialize(b: B): F[js.Any] = outer.serialize(g(b))
      def deserialize(serialized: js.Any): F[B] = outer.deserialize(serialized).imap(f)(g)
    }

}

object Serializer {

  implicit def invariant[F[_]: Invariant]: Invariant[Serializer[F, *]] =
    new Invariant[Serializer[F, *]] {
      def imap[A, B](fa: Serializer[F, A])(f: A => B)(g: B => A): Serializer[F, B] =
        fa.imap(f)(g)
    }

  implicit def fromCirceCodec[F[_], A](implicit
      F: Sync[F],
      decoder: Decoder[A],
      encoder: Encoder[A]
  ): Serializer[F, A] = new Serializer[F, A] {
    def serialize(a: A): F[js.Any] = F.delay(a.asJsAny)
    def deserialize(serialized: js.Any): F[A] = decodeJs[A](serialized).liftTo[F]
  }

}
