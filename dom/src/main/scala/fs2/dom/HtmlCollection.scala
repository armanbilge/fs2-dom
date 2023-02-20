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

import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class HtmlCollection[F[_], A] private {
  def length: F[Int]

  def apply(index: Int): F[Option[A]]

  def apply(key: String): F[Option[A]]
}

object HtmlCollection {
  private[dom] def apply[F[_], A](
      collection: dom.HTMLCollection[A]
  )(implicit F: Sync[F]): HtmlCollection[F, A] =
    new HtmlCollection[F, A] {
      def length = F.delay(collection.length)
      def apply(index: Int) = F.delay(Option(collection.item(index)))
      def apply(key: String) = F.delay(Option(collection.namedItem(key)))
    }
}
