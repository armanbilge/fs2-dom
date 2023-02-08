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
import cats.effect.kernel.Concurrent
import cats.effect.kernel.Ref
import cats.effect.std.Queue
import cats.effect.syntax.all._
import cats.syntax.all._
import fs2.Stream
import fs2.concurrent.Signal
import org.scalajs.dom
import org.scalajs.dom.EventListenerOptions
import org.scalajs.dom.ScrollRestoration

abstract class History[F[_], S] private {

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

object History {
  private[dom] def apply[F[_], S](window: dom.Window, history: dom.History)(implicit
      F: Async[F],
      serializer: Serializer[F, S]
  ): History[F, S] =
    new History[F, S] {

      def state = new Signal[F, Option[S]] {
        override def getAndDiscreteUpdates(implicit ev: Concurrent[F]) =
          getAndDiscreteUpdatesImpl

        private[this] def getAndDiscreteUpdatesImpl =
          for {
            queue <- Queue.circularBuffer[F, dom.PopStateEvent](1).toResource
            _ <- events[F, dom.PopStateEvent](window, "popstate")
              .foreach(queue.offer(_))
              .compile
              .drain
              .background
            got <- get.toResource
            updates = Stream
              .repeatEval {
                for {
                  event <- queue.take
                  state <- serializer.deserialize(event.state)
                } yield Some(state)
              }
          } yield (got, updates)

        def discrete = Stream.resource(getAndDiscreteUpdatesImpl).flatMap { case (got, updates) =>
          Stream.emit(got) ++ updates
        }

        def get = OptionT(F.delay(Option(history.state)))
          .semiflatMap(serializer.deserialize(_))
          .value

        def continuous = Stream.repeatEval(get)
      }

      def length = new Signal[F, Int] {
        def discrete = state.discrete.evalMap(_ => get)
        def get = F.delay(history.length)
        def continuous = Stream.repeatEval(get)
      }

      def scrollRestoration = new WrappedRef(
        () => history.scrollRestoration,
        history.scrollRestoration = _
      )

      def forward = asyncPopState(history.forward())
      def back = asyncPopState(history.back())
      def go = asyncPopState(history.go())
      def go(delta: Int) = asyncPopState(history.go(delta))

      def pushState(state: S) =
        serializer.serialize(state).flatMap(s => F.delay(history.pushState(s, "")))
      def pushState(state: S, url: String) =
        serializer.serialize(state).flatMap(s => F.delay(history.pushState(s, "", url)))

      def replaceState(state: S) =
        serializer.serialize(state).flatMap(s => F.delay(history.replaceState(s, "")))
      def replaceState(state: S, url: String) =
        serializer.serialize(state).flatMap(s => F.delay(history.replaceState(s, "", url)))

      def asyncPopState(thunk: => Unit): F[Unit] = F.async_[Unit] { cb =>
        window.addEventListener[dom.PopStateEvent](
          "popstate",
          (_: Any) => cb(Either.unit),
          new EventListenerOptions {
            once = true
          }
        )
        thunk
      }
    }
}
