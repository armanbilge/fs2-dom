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

abstract class Window[F[_]] private {

  def history[S](implicit serializer: Serializer[F, S]): History[F, S]

  def localStorage: Storage[F]

  def location: Location[F]

  def navigator: Navigator[F]

  def sessionStorage: Storage[F]

  def storageEvents: Stream[F, StorageEvent[F]]

}

object Window {

  def apply[F[_]](implicit F: Async[F]): Window[F] =
    apply(dom.window)

  def apply[F[_]](window: dom.Window)(implicit F: Async[F]): Window[F] =
    new Window[F] {

      def history[S](implicit serializer: Serializer[F, S]) = History(window, window.history)

      def localStorage = Storage(window.localStorage)

      def location = Location(window.location)

      def navigator = Navigator(window.navigator)

      def sessionStorage = Storage(window.sessionStorage)

      def storageEvents: Stream[F, StorageEvent[F]] =
        events[F, dom.StorageEvent](window, "storage").map(StorageEvent(_))

    }

}
