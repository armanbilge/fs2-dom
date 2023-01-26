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

import cats.effect.kernel.Sync
import org.scalajs.dom

abstract class UIEvent[F[_]] private[dom] extends Event[F] {

  def detail: Long

  def view: dom.Window

}

object UIEvent {
  def apply[F[_]](event: dom.UIEvent)(implicit F: Sync[F]): UIEvent[F] =
    new WrappedUIEvent(event)
}

private final class WrappedUIEvent[F[_]](val event: dom.UIEvent)(implicit val F: Sync[F])
    extends UIEventImpl[F]

private trait UIEventImpl[F[_]] extends UIEvent[F] with EventImpl[F] {
  def event: dom.UIEvent
  implicit def F: Sync[F]

  def detail = event.detail.toLong
  def view = event.view
}
