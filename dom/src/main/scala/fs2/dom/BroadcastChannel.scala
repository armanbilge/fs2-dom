package fs2.dom

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import fs2.Stream
import org.scalajs.dom

abstract class BroadcastChannel[F[_], A] private {
  def name: String

  def postMessage(a: A): F[Unit]

  def messages: Stream[F, MessageEvent[F, A]]
}

object BroadcastChannel {

  def apply[F[_], A](
      channelName: String
  )(implicit F: Async[F], A: Serializer[A]): Resource[F, BroadcastChannel[F, A]] =
    Resource.make(F.delay(new dom.BroadcastChannel(channelName)))(bc => F.delay(bc.close())).map {
      bc =>
        new BroadcastChannel[F, A] {
          def name = channelName
          def postMessage(a: A) = F.delay(bc.postMessage(A.serialize(a)))
          def messages =
            events[F, dom.MessageEvent](bc, "message").evalMap(MessageEvent.deserialize(_))
        }
    }

}
