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

import cats.data.OptionT
import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.syntax.all._
import fs2.concurrent.Signal
import fs2.Stream
import org.scalajs.dom.EventListenerOptions
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.ScrollRestoration
import org.scalajs.dom.window

trait HistoryDsl[F[_], S] {

  def state: Signal[F, Option[S]]
  def length: Signal[F, Int]
  def scrollRestoration: Ref[F, ScrollRestoration]

  def forward: F[Unit]
  def back: F[Unit]
  def go: F[Unit]
  def go(delta: Int): F[Unit]

  def pushState(state: S): F[Unit]
  def pushState(state: S, url: String): F[Unit]

  def replaceState(state: S): F[Unit]
  def replaceState(state: S, url: String): F[Unit]

}

object HistoryDsl {
  implicit def interpreter[F[_], S](implicit F: Async[F], serializer: Serializer[F, S]): HistoryDsl[F, S] =
    new HistoryDsl[F, S] {

      def state = new Signal[F, Option[S]] {
        def discrete =
          Stream.resource(eventsResource[F, PopStateEvent](window, "popstate")).flatMap { events =>
            Stream.eval(get) ++ events.evalMap(e => serializer.deserialize(e.state).map(Some(_)))
          }

        def get = OptionT(F.delay(Option(window.history.state)))
          .semiflatMap(serializer.deserialize(_))
          .value

        def continuous = Stream.repeatEval(get)
      }

      def length = new Signal[F, Int] {
        def discrete = state.discrete.evalMap(_ => get)
        def get = F.delay(window.history.length)
        def continuous = Stream.repeatEval(get)
      }

      def scrollRestoration = new WrappedRef[F, ScrollRestoration] {
        def unsafeGet(): ScrollRestoration = window.history.scrollRestoration
        def unsafeSet(sr: ScrollRestoration): Unit =
          window.history.scrollRestoration = sr
      }

      def forward = asyncPopState(window.history.forward())
      def back = asyncPopState(window.history.back())
      def go = asyncPopState(window.history.go())
      def go(delta: Int) = asyncPopState(window.history.go(delta))

      def pushState(state: S) =
        serializer.serialize(state).flatMap(s => F.delay(window.history.pushState(s, "")))
      def pushState(state: S, url: String) =
        serializer.serialize(state).flatMap(s => F.delay(window.history.pushState(s, "", url)))

      def replaceState(state: S) =
        serializer.serialize(state).flatMap(s => F.delay(window.history.replaceState(s, "")))
      def replaceState(state: S, url: String) =
        serializer.serialize(state).flatMap(s => F.delay(window.history.replaceState(s, "", url)))

      def asyncPopState(thunk: => Unit): F[Unit] = F.async_[Unit] { cb =>
        window.addEventListener[PopStateEvent](
          "popstate",
          (_: Any) => cb(Either.unit),
          new EventListenerOptions {
            once = true
          }
        )
        thunk
      }
    }

  def apply[F[_], S](implicit ev: HistoryDsl[F, S]) = ev

}
