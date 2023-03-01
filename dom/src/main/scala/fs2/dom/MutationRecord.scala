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

  def previousSibling: Option[Node[F]]

  def nextSibling: Option[Node[F]]

  def attributeName: Option[String]

  def attributeNamespace: Option[String]

  def oldValue: Option[String]

}

object MutationRecord {

  private[dom] def fromJS[F[_]](record: dom.MutationRecord) = new MutationRecord[F] {

    override def `type`: String = record.`type`

    override def target: Node[F] = record.target.asInstanceOf[Node[F]]

    override def addedNodes: List[Node[F]] =
      record.addedNodes.toList.asInstanceOf[List[Node[F]]]

    override def removedNodes: List[Node[F]] =
      record.removedNodes.toList.asInstanceOf[List[Node[F]]]

    override def previousSibling: Option[Node[F]] = Option(
      record.previousSibling.asInstanceOf[Node[F]]
    )

    override def nextSibling: Option[Node[F]] =
      Option(record.nextSibling.asInstanceOf[Node[F]])

    override def attributeName: Option[String] =
      Option(record.attributeName)

    override def attributeNamespace: Option[String] =
      Option(record.attributeNamespace)

    override def oldValue: Option[String] = Option(record.oldValue)

  }

}
