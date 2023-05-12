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
import fs2.Stream
import org.scalajs.dom

import scala.concurrent.duration._

abstract class Window[F[_]] private {

  def history[S: Serializer]: History[F, S]

  def localStorage: Storage[F]

  def location: Location[F]

  def navigator: Navigator[F]

  def sessionStorage: Storage[F]

  def storageEvents: Stream[F, StorageEvent[F]]

  implicit def given_Dom_F: Dom[F]

  def document: HtmlDocument[F]

  def requestAnimationFrame: F[FiniteDuration]

}

object Window {

  def apply[F[_]](implicit F: Async[F]): Window[F] =
    apply(dom.window)

  private def apply[F[_]](window: dom.Window)(implicit F: Async[F]): Window[F] =
    new Window[F] {

      def history[S: Serializer] = History(window, window.history)

      def localStorage = Storage(window.localStorage)

      def location = Location(window.location)

      def navigator = Navigator(window.navigator)

      def sessionStorage = Storage(window.sessionStorage)

      def storageEvents: Stream[F, StorageEvent[F]] =
        events[F, dom.StorageEvent](window, "storage").map(StorageEvent(_))

      implicit def given_Dom_F: Dom[F] = Dom.forAsync

      def document: HtmlDocument[F] = window.document.asInstanceOf[HtmlDocument[F]]

      def requestAnimationFrame: F[FiniteDuration] = F.async[FiniteDuration] { cb =>
        F.delay {
          val id = window.requestAnimationFrame { timestamp =>
            cb(Right(timestamp.millis))
          }
          Some(F.delay(window.cancelAnimationFrame(id)))
        }
      }

    }

}
