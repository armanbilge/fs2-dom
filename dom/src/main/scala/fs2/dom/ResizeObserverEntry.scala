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

import org.scalajs.dom

abstract class ResizeObserverEntry[F[_]] private[dom] {

  def target: Element[F]

  def borderBoxSize: List[dom.ResizeObserverSize]

  def contentBoxSize: List[dom.ResizeObserverSize]

  def contentRect: dom.DOMRectReadOnly

}

object ResizeObserverEntry {

  private[dom] def fromJS[F[_]](entry: dom.ResizeObserverEntry) = new ResizeObserverEntry[F] {

    def target: Element[F] = entry.target.asInstanceOf[Element[F]]

    def borderBoxSize: List[dom.ResizeObserverSize] = entry.borderBoxSize.toList

    def contentBoxSize: List[dom.ResizeObserverSize] = entry.contentBoxSize.toList

    def contentRect: dom.DOMRectReadOnly = entry.contentRect

  }

}
