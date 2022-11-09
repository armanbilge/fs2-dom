package fs2.dom

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.all._
import fs2.Stream
import org.scalajs.dom.Blob

trait BlobDsl[F[_]] {
  def readBlob(blob: F[Blob]): Stream[F, Byte]
}

object BlobDsl {
  implicit def interpreter[F[_] : Async]: BlobDsl[F] = new BlobDsl[F] {
    override def readBlob(blob: F[Blob]): Stream[F, Byte] =
      ReadableStreamDsl[F].readReadableStream(blob.flatMap(b => Sync[F].delay(b.stream())), false)
  }

  def apply[F[_]](implicit ev: BlobDsl[F]) = ev
}
