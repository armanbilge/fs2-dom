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

import scala.concurrent.duration._

abstract class Event[F[_]] private[dom] {

  def bubbles: Boolean

  def cancelable: Boolean

  // def composed: Boolean

  def defaultPrevented: F[Boolean]

  def eventPhase: F[Int]

  def isTrusted: Boolean

  def timeStamp: F[FiniteDuration]

  def `type`: String

  def preventDefault: F[Unit]

  def stopImmediatePropagation: F[Unit]

  def stopPropagation: F[Unit]

}

object Event {
  def apply[F[_]](event: dom.Event)(implicit F: Sync[F]): Event[F] =
    new WrappedEvent(event)
}

private final class WrappedEvent[F[_]](val event: dom.Event)(implicit val F: Sync[F])
    extends EventImpl[F]

private trait EventImpl[F[_]] extends Event[F] {
  def event: dom.Event
  implicit def F: Sync[F]

  def bubbles = event.bubbles
  def cancelable = event.cancelable
  // def composed = event.composed
  def defaultPrevented = F.delay(event.defaultPrevented)
  def eventPhase = F.delay(event.eventPhase)
  def isTrusted = event.isTrusted
  def timeStamp = F.delay(event.timeStamp.millis)
  def `type` = event.`type`
  def preventDefault = F.delay(event.preventDefault())
  def stopImmediatePropagation = F.delay(event.preventDefault())
  def stopPropagation = F.delay(event.stopPropagation())
}
