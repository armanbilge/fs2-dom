package fs2.dom

import cats.syntax.all._
import org.scalajs.dom
import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync

abstract class MutationObserver[F[_]] private[dom] {

  def observe(target: Node[F], options: dom.MutationObserverInit): F[Unit]

  def disconnect: F[Unit]

  def takeRecords: F[List[dom.MutationRecord]]

}

object MutationObserver {

  def apply[F[_]](
      callback: (List[dom.MutationRecord], MutationObserver[F]) => F[Unit]
  )(implicit F: Async[F]): Resource[F, MutationObserver[F]] = for {
    dispatcher <- Dispatcher.parallel[F]
    jsObserver <- Resource.make(
      F.delay(
        new dom.MutationObserver((a, b) =>
          dispatcher.unsafeRunAndForget(callback(a.toList, fromJsObserver(b)))
        )
      )
    )(obs => F.delay(obs.disconnect()))
  } yield fromJsObserver(jsObserver)

  private def fromJsObserver[F[_]](
      jsObserver: dom.MutationObserver
  )(implicit F: Sync[F]): MutationObserver[F] =
    new MutationObserver[F] {

      def observe(target: Node[F], options: dom.MutationObserverInit): F[Unit] =
        F.delay(jsObserver.observe(target.asInstanceOf[dom.Node], options))

      def disconnect: F[Unit] = F.delay(jsObserver.disconnect())

      def takeRecords: F[List[dom.MutationRecord]] =
        F.delay(jsObserver.takeRecords()).map(_.toList)

    }

}
