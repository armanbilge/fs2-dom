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

  def eventPhase: F[Event.Phase]

  def isTrusted: Boolean

  def timeStamp: F[FiniteDuration]

  def `type`: String

  def preventDefault: F[Unit]

  def stopImmediatePropagation: F[Unit]

  def stopPropagation: F[Unit]

}

object Event {

  sealed abstract class Phase
  object Phase {
    object None extends Phase
    object Capturing extends Phase
    object AtTarget extends Phase
    object Bubbling extends Phase

    private[dom] def fromInt(phase: Int): Phase = phase match {
      case 0 => None
      case 1 => Capturing
      case 2 => AtTarget
      case 3 => Bubbling
    }
  }

}

private[dom] class WrappedEvent[F[_]](event: dom.Event)(implicit F: Sync[F]) extends Event[F] {
  def bubbles = event.bubbles
  def cancelable = event.cancelable
  // def composed = event.composed
  def defaultPrevented = F.delay(event.defaultPrevented)
  def eventPhase = F.delay(Event.Phase.fromInt(event.eventPhase))
  def isTrusted = event.isTrusted
  def timeStamp = F.delay(event.timeStamp.millis)
  def `type` = event.`type`
  def preventDefault = F.delay(event.preventDefault())
  def stopImmediatePropagation = F.delay(event.preventDefault())
  def stopPropagation = F.delay(event.stopPropagation())
}
