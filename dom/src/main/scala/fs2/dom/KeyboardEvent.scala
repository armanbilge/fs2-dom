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

abstract class KeyboardEvent[F[_]] private[dom] extends UIEvent[F] {

  def altKey: Boolean

  def code: String

  def ctrlKey: Boolean

  // def isComposing: Boolean

  def key: String

  def location: Int

  def metaKey: Boolean

  def repeat: Boolean

  def shiftKey: Boolean

  def getModifierState(key: String): F[Boolean]

}

object KeyboardEvent {
  def apply[F[_]](event: dom.KeyboardEvent)(implicit F: Sync[F]): KeyboardEvent[F] =
    new WrappedKeyboardEvent(event)
}

private final class WrappedKeyboardEvent[F[_]](val event: dom.KeyboardEvent)(implicit
    val F: Sync[F]
) extends KeyboardEventImpl[F]

private trait KeyboardEventImpl[F[_]] extends KeyboardEvent[F] with UIEventImpl[F] {
  def event: dom.KeyboardEvent
  implicit def F: Sync[F]

  def altKey = event.altKey
  def code = event.asInstanceOf[scalajs.js.Dynamic].code.asInstanceOf[String]
  def ctrlKey = event.ctrlKey
  // def isComposing = event.isComposing
  def key = event.key
  def location = event.location
  def metaKey = event.metaKey
  def repeat = event.repeat
  def shiftKey = event.shiftKey
  def getModifierState(key: String) = F.delay(event.getModifierState(key))
}
