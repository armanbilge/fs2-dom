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

import cats.effect.kernel.Ref
import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class Location[F[_]] private {

  def href: Ref[F, String]

  def protocol: Ref[F, String]

  def host: Ref[F, String]

  def hostname: Ref[F, String]

  def port: Ref[F, String]

  def pathname: Ref[F, String]

  def search: Ref[F, String]

  def hash: Ref[F, String]

  def origin: F[String]

  def assign(url: String): F[Unit]

  def reload: F[Unit]

  def replace(url: String): F[Unit]

}

object Location {

  def apply[F[_]: Sync]: Location[F] =
    apply(dom.window.location)

  private def apply[F[_]](location: dom.Location)(implicit F: Sync[F]): Location[F] =
    new Location[F] {

      def href = new PropRef(location, "href")

      def protocol = new PropRef(location, "protocol")

      def host = new PropRef(location, "host")

      def hostname = new PropRef(location, "hostname")

      def port = new PropRef(location, "port")

      def pathname = new PropRef(location, "pathname")

      def search = new PropRef(location, "search")

      def hash = new PropRef(location, "hash")

      def origin = F.delay(location.origin.asInstanceOf[String])

      def assign(url: String) = F.delay(location.assign(url))

      def reload: F[Unit] = F.delay(location.reload())

      def replace(url: String) = F.delay(location.replace(url))

    }

}
