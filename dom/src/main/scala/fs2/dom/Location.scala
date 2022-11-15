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

      def href = new WrappedRef[F, String] {
        def unsafeGet() = location.href
        def unsafeSet(s: String): Unit = location.href = s
      }

      def protocol = new WrappedRef[F, String] {
        def unsafeGet() = location.protocol
        def unsafeSet(s: String): Unit = location.protocol = s
      }

      def host = new WrappedRef[F, String] {
        def unsafeGet() = location.host
        def unsafeSet(s: String): Unit = location.host = s
      }

      def hostname = new WrappedRef[F, String] {
        def unsafeGet() = location.hostname
        def unsafeSet(s: String): Unit = location.hostname = s
      }

      def port = new WrappedRef[F, String] {
        def unsafeGet() = location.port
        def unsafeSet(s: String): Unit = location.port = s
      }

      def pathname = new WrappedRef[F, String] {
        def unsafeGet() = location.pathname
        def unsafeSet(s: String): Unit = location.pathname = s
      }

      def search = new WrappedRef[F, String] {
        def unsafeGet() = location.search
        def unsafeSet(s: String): Unit = location.search = s
      }

      def hash = new WrappedRef[F, String] {
        def unsafeGet() = location.hash
        def unsafeSet(s: String): Unit = location.hash = s
      }

      def origin = F.delay(location.origin.asInstanceOf[String])

      def assign(url: String) = F.delay(location.assign(url))

      def reload: F[Unit] = F.delay(location.reload())

      def replace(url: String) = F.delay(location.replace(url))

    }

}
