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

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.all._
import org.scalajs.dom.Blob
import org.scalajs.dom.ReadableStream

import org.scalajs.dom.Event
import org.scalajs.dom.EventListenerOptions
import org.scalajs.dom.EventTarget
import scala.scalajs.js.typedarray.Uint8Array

package object dom {

  def readBlob[F[_]](blob: F[Blob])(implicit F: Async[F]): Stream[F, Byte] =
    readReadableStream(blob.flatMap(b => F.delay(b.stream())))

  def readReadableStream[F[_]: Async](
      readableStream: F[ReadableStream[Uint8Array]],
      cancelAfterUse: Boolean = true
  ): Stream[F, Byte] = StreamConverters.readReadableStream(readableStream, cancelAfterUse)

  def toReadableStream[F[_]: Async]: Pipe[F, Byte, ReadableStream[Uint8Array]] =
    StreamConverters.toReadableStream

  def toReadableStreamResource[F[_]: Async](
      stream: Stream[F, Byte]
  ): Resource[F, ReadableStream[Uint8Array]] =
    stream.through(toReadableStream).compile.resource.lastOrError

  def events[F[_]: Async, E <: Event](
      target: EventTarget,
      `type`: String,
      options: EventListenerOptions
  ): Stream[F, E] =
    Stream.resource(EventTargetHelpers.listen(target, `type`, options)).flatten

  def eventsResource[F[_]: Async, E <: Event](
      target: EventTarget,
      `type`: String,
      options: EventListenerOptions
  ): Resource[F, Stream[F, E]] =
    EventTargetHelpers.listen(target, `type`, options)

}
