package fs2
package dom

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import org.scalajs.dom

object AbortController {
  def apply[F[_]](implicit F: Sync[F]): Resource[F, dom.AbortSignal] =
    Resource
      .make(F.delay(new dom.AbortController))(ctrl => F.delay(ctrl.abort()))
      .map(_.signal)
}
