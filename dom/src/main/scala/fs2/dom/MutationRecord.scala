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

abstract class MutationRecord[F[_]] private[dom] {

  def `type`: String

  def target: Node[F]

  def addedNodes: List[Node[F]]

  def removedNodes: List[Node[F]]

  def previousSibling: Node[F]

  def nextSibling: Node[F]

  def attributeName: String

  def attributeNamespace: String

  def oldValue: String

}

object MutationRecord {

  private[dom] def fromJS[F[_]](record: dom.MutationRecord) = new MutationRecord[F] {

    override def `type`: String = record.`type`

    override def target: Node[F] = record.target.asInstanceOf[Node[F]]

    override def addedNodes: List[Node[F]] =
      record.addedNodes.toList.map(_.asInstanceOf[Node[F]])

    override def removedNodes: List[Node[F]] =
      record.removedNodes.toList.map(_.asInstanceOf[Node[F]])

    override def previousSibling: Node[F] = record.previousSibling.asInstanceOf[Node[F]]

    override def nextSibling: Node[F] = record.nextSibling.asInstanceOf[Node[F]]

    override def attributeName: String = record.attributeName

    override def attributeNamespace: String = record.attributeNamespace

    override def oldValue: String = record.oldValue

  }

}
