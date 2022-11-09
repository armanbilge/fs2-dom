package fs2.dom

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.syntax.all._
import fs2.Chunk
import fs2.Pipe
import fs2.Stream
import org.scalajs.dom.ReadableStream
import org.scalajs.dom.ReadableStreamType
import org.scalajs.dom.ReadableStreamUnderlyingSource
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

trait ReadableStreamDsl[F[_]] {
  def readReadableStream(readableStream: F[ReadableStream[Uint8Array]],
                         cancelAfterUse: Boolean): Stream[F, Byte]

  def toReadableStream: Pipe[F, Byte, ReadableStream[Uint8Array]]
}

object ReadableStreamDsl {
  implicit def interpreter[F[_] : Async]: ReadableStreamDsl[F] = new ReadableStreamDsl[F] {
    override def readReadableStream(readableStream: F[ReadableStream[Uint8Array]],
                                    cancelAfterUse: Boolean): Stream[F, Byte] = {
      def read(readableStream: ReadableStream[Uint8Array]) =
        Stream.bracket(Sync[F].delay(readableStream.getReader()))(r => Sync[F].delay(r.releaseLock())).flatMap {
          reader =>
            Stream.unfoldChunkEval(reader) { reader =>
              Async[F].fromPromise(Sync[F].delay(reader.read())).map { chunk =>
                if (chunk.done)
                  None
                else
                  Some((fs2.Chunk.uint8Array(chunk.value), reader))
              }
            }
        }

      if (cancelAfterUse)
        Stream.bracketCase(readableStream)(cancelReadableStream(_, _)).flatMap(read(_))
      else
        Stream.eval(readableStream).flatMap(read(_))
    }

    override def toReadableStream: Pipe[F, Byte, ReadableStream[Uint8Array]] = (in: Stream[F, Byte]) =>
      Stream.resource(Dispatcher.sequential).flatMap { dispatcher =>
        Stream
          .eval(Queue.synchronous[F, Option[Chunk[Byte]]])
          .flatMap { chunks =>
            Stream
              .eval(Sync[F].delay {
                val source = new ReadableStreamUnderlyingSource[Uint8Array] {
                  `type` = ReadableStreamType.bytes
                  pull = js.defined { controller =>
                    dispatcher.unsafeToPromise {
                      chunks.take.flatMap {
                        case Some(chunk) =>
                          Sync[F].delay(controller.enqueue(chunk.toUint8Array))
                        case None => Sync[F].delay(controller.close())
                      }
                    }
                  }
                }
                ReadableStream[Uint8Array](source)
              })
              .concurrently(in.enqueueNoneTerminatedChunks(chunks))
          }
      }
  }

  private[this] def cancelReadableStream[F[_], A](
      rs: ReadableStream[A],
      exitCase: Resource.ExitCase
  )(implicit F: Async[F]): F[Unit] = F.fromPromise {
    F.delay {
      // Best guess: Firefox internally locks a ReadableStream after it is "drained"
      // This checks if the stream is locked before canceling it to avoid an error
      if (!rs.locked) exitCase match {
        case Resource.ExitCase.Succeeded =>
          rs.cancel(js.undefined)
        case Resource.ExitCase.Errored(ex) =>
          rs.cancel(ex.toString())
        case Resource.ExitCase.Canceled =>
          rs.cancel(js.undefined)
      }
      else js.Promise.resolve[Unit](())
    }
  }

  def apply[F[_]](implicit ev: ReadableStreamDsl[F]) = ev
}
