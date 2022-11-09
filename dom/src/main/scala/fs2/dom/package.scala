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

package fs2

import cats.effect.kernel.MonadCancel
import cats.effect.kernel.Resource
import cats.MonadThrow
import org.scalajs.dom.Event
import org.scalajs.dom.EventTarget
import org.scalajs.dom.ReadableStream
import scala.scalajs.js.typedarray.Uint8Array

package object dom {

  def toReadableStreamResource[F[_]: ReadableStreamDsl : Compiler[*[_], F] : Compiler[*[_], Resource[F, *]] : MonadThrow](
      stream: Stream[F, Byte]
  ): Resource[F, ReadableStream[Uint8Array]] =
    stream
      .through(ReadableStreamDsl[F].toReadableStream)
      .compile[F, F, ReadableStream[Uint8Array]]
      .resource
      .lastOrError

  def events[F[_]: EventTargetDsl : MonadCancel[*[_], Throwable], E <: Event](target: EventTarget, `type`: String): Stream[F, E] =
    Stream.resource(EventTargetDsl[F].listen(target, `type`)).flatten

  def eventsResource[F[_]: EventTargetDsl, E <: Event](
      target: EventTarget,
      `type`: String
  ): Resource[F, Stream[F, E]] =
    EventTargetDsl[F].listen(target, `type`)

}
