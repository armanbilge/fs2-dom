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

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import fs2.Stream
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.EventListenerOptions

opaque type Dom[F[_]] = Async[F]
object Dom {
  implicit inline def forIO: Dom[IO] = IO.asyncForIO
  inline def forAsync[F[_]](using F: Async[F]): Dom[F] = F
}

opaque type Node[F[_]] = dom.Node
object Node {
  extension [F[_]](node: Node[F]) {

    def firstChild(using F: Dom[F]): F[Option[Node[F]]] =
      F.delay(Option(node.firstChild))

    def appendChild(child: Node[F])(using F: Dom[F]): F[Unit] = F.delay {
      node.appendChild(child)
      ()
    }

    def removeChild(child: Node[F])(using F: Dom[F]): F[Unit] = F.delay {
      node.removeChild(child)
      ()
    }

    def replaceChild(newChild: Node[F], oldChild: Node[F])(using F: Dom[F]): F[Unit] = F.delay {
      node.replaceChild(newChild, oldChild)
      ()
    }

    def addEventListener[T <: Event](`type`: String, options: EventListenerOptions)(using
        F: Dom[F]
    ): Resource[F, Stream[F, T]] =
      EventTargetHelpers.listen(node, `type`, options)

    def attributes(using F: Dom[F]): F[NamedNodeMap[F]] =
      F.delay(node.attributes)

    def nodeType: Int = node.nodeType

    def previousSibling(using F: Dom[F]): F[Node[F]] =
      F.delay(node.previousSibling)

    def localName(using F: Dom[F]): F[String] = F.delay(node.localName)

    def namespaceURI(using F: Dom[F]): F[Option[String]] = F.delay(Option(node.namespaceURI))

    def textContent(using F: Dom[F]): Ref[F, String] =
      new WrappedRef {
        def unsafeGet() = node.textContent
        def unsafeSet(s: String) = node.textContent = s
      }

    def parentNode(using F: Dom[F]): F[Node[F]] = F.delay(node.parentNode)

    def nextSibling(using F: Dom[F]): F[Option[Node[F]]] = F.delay(Option(node.nextSibling))

    def nodeValue(using F: Dom[F]): Ref[F, String] =
      new WrappedRef {
        def unsafeGet() = node.nodeValue
        def unsafeSet(s: String) = node.nodeValue = s
      }

    def lastChild(using F: Dom[F]): F[Option[Node[F]]] = F.delay(Option(node.lastChild))

    def childNodes(using F: Dom[F]): F[NodeList[F, Node[F]]] = F.delay(node.childNodes)

    def nodeName: Option[String] = Option(node.nodeName)

    def ownerDocument(using F: Dom[F]): F[Option[Document[F]]] = F.delay(Option(node.ownerDocument))

    def lookupPrefix(namespaceURI: String)(using F: Dom[F]): F[Option[String]] =
      F.delay(Option(node.lookupPrefix(namespaceURI)))

    def isDefaultNamespace(namespaceURI: String)(using F: Dom[F]): F[Boolean] =
      F.delay(node.isDefaultNamespace(namespaceURI))

    def compareDocumentPosition(other: Node[F])(using F: Dom[F]): F[Int] =
      F.delay(node.compareDocumentPosition(other))

    def normalize(using F: Dom[F]): F[Unit] = F.delay(node.normalize())

    def isSameNode(other: Node[F])(using F: Dom[F]): F[Boolean] = F.delay(node.isSameNode(other))

    def contains(otherNode: Node[F])(using F: Dom[F]): F[Boolean] =
      F.delay(node.contains(otherNode))

    def hasAttributes(using F: Dom[F]): F[Boolean] = F.delay(node.hasAttributes())

    def lookupNamespaceURI(prefix: String)(using F: Dom[F]): F[Option[String]] =
      F.delay(Option(node.lookupNamespaceURI(prefix)))

    def cloneNode(deep: Boolean)(using F: Dom[F]): F[Node[F]] = F.delay(node.cloneNode(deep))

    def hasChildNodes(using F: Dom[F]): F[Boolean] = F.delay(node.hasChildNodes())

    def insertBefore(newChild: Node[F], refChild: Node[F])(using F: Dom[F]): F[Node[F]] =
      F.delay(node.insertBefore(newChild, refChild))

    def baseURI(using F: Dom[F]): F[String] = F.delay(node.baseURI)

    def isConnected(using F: Dom[F]): F[Boolean] = F.delay(node.isConnected)

    def innerText(using F: Dom[F]): Ref[F, String] =
      new WrappedRef {
        def unsafeGet() = node.innerText
        def unsafeSet(s: String) = node.innerText = s
      }
  }

  val ENTITY_REFERENCE_NODE: Int = dom.Node.ENTITY_REFERENCE_NODE

  val ATTRIBUTE_NODE: Int = dom.Node.ATTRIBUTE_NODE

  val DOCUMENT_FRAGMENT_NODE: Int = dom.Node.DOCUMENT_FRAGMENT_NODE

  val TEXT_NODE: Int = dom.Node.TEXT_NODE

  val ELEMENT_NODE: Int = dom.Node.ELEMENT_NODE

  val COMMENT_NODE: Int = dom.Node.COMMENT_NODE

  val DOCUMENT_POSITION_DISCONNECTED: Int = dom.Node.DOCUMENT_POSITION_DISCONNECTED

  val DOCUMENT_POSITION_CONTAINED_BY: Int = dom.Node.DOCUMENT_POSITION_CONTAINED_BY

  val DOCUMENT_POSITION_CONTAINS: Int = dom.Node.DOCUMENT_POSITION_CONTAINS

  val DOCUMENT_TYPE_NODE: Int = dom.Node.DOCUMENT_TYPE_NODE

  val DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC: Int =
    dom.Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC

  val DOCUMENT_NODE: Int = dom.Node.DOCUMENT_NODE

  val ENTITY_NODE: Int = dom.Node.ENTITY_NODE

  val PROCESSING_INSTRUCTION_NODE: Int = dom.Node.PROCESSING_INSTRUCTION_NODE

  val CDATA_SECTION_NODE: Int = dom.Node.CDATA_SECTION_NODE

  val NOTATION_NODE: Int = dom.Node.NOTATION_NODE

  val DOCUMENT_POSITION_FOLLOWING: Int = dom.Node.DOCUMENT_POSITION_FOLLOWING

  val DOCUMENT_POSITION_PRECEDING: Int = dom.Node.DOCUMENT_POSITION_PRECEDING
}

opaque type DOMList[F[_], +A] = dom.DOMList[A]

object DOMList {
  extension [F[_], A <: Node[F]](domList: DOMList[F, A]) {
    def apply(index: Int)(using F: Dom[F]): F[A] = F.delay(domList(index))
    def length(using F: Dom[F]): F[Int] = F.delay(domList.length)
    def toList: List[A] = dom.DOMList.domListAsSeq(domList).toList
  }
}

opaque type NodeList[F[_], +A <: Node[F]] <: DOMList[F, A] = dom.NodeList[A]

object NodeList {
  extension [F[_], A <: Node[F]](nodeList: NodeList[F, A]) {
    def item(index: Int)(using F: Dom[F]): F[A] = F.delay(nodeList.item(index))
  }
}

opaque type NamedNodeMap[F[_]] = dom.NamedNodeMap

object NamedNodeMap {
  extension [F[_]](nodeMap: NamedNodeMap[F]) {
    def length(using F: Dom[F]): F[Int] = F.delay(nodeMap.length)

    def removeNamedItemNS(namespaceURI: String, localName: String)(using F: Dom[F]): F[Attr[F]] =
      F.delay(nodeMap.removeNamedItemNS(namespaceURI, localName))

    def item(index: Int)(using F: Dom[F]): F[Attr[F]] = F.delay(nodeMap.item(index))

    def apply(index: Int)(using F: Dom[F]): F[Attr[F]] = F.delay(nodeMap(index))

    def update(index: Int, v: Attr[F])(using F: Dom[F]): F[Unit] = F.delay(nodeMap.update(index, v))

    def removeNamedItem(name: String)(using F: Dom[F]): F[Attr[F]] =
      F.delay(nodeMap.removeNamedItem(name))

    def getNamedItem(name: String)(using F: Dom[F]): F[Option[Attr[F]]] =
      F.delay(Option(nodeMap.getNamedItem(name)))

    def setNamedItem(arg: Attr[F])(using F: Dom[F]): F[Attr[F]] = F.delay(nodeMap.setNamedItem(arg))

    def getNamedItemNS(namespaceURI: String, localName: String)(using
        F: Dom[F]
    ): F[Option[Attr[F]]] =
      F.delay(Option(nodeMap.getNamedItemNS(namespaceURI, localName)))

    def setNamedItemNS(arg: Attr[F])(using F: Dom[F]): F[Attr[F]] =
      F.delay(nodeMap.setNamedItemNS(arg))
  }
}

opaque type Attr[F[_]] = dom.Attr

object Attr {
  extension [F[_]](attr: Attr[F]) {
    def ownerElement(using F: Dom[F]): F[Element[F]] = F.delay(attr.ownerElement)
    def value(using F: Dom[F]): Ref[F, String] =
      new WrappedRef {
        def unsafeGet() = attr.value
        def unsafeSet(s: String) = attr.value = s
      }

    def name: String = attr.name

    def prefix: String = attr.prefix
  }
}

opaque type Document[F[_]] <: Node[F] = dom.Document
object Document {
  def apply[F[_]: Dom]: Document[F] = dom.window.document

  extension [F[_]](document: Document[F]) {

    def createElement(tagName: String)(using F: Dom[F]): F[Element[F]] =
      F.delay(document.createElement(tagName))

    def getElementById(id: String)(using F: Dom[F]): F[Option[HtmlElement[F]]] =
      F.delay(Option(document.getElementById(id).asInstanceOf[HtmlElement[F]]))

  }
}

opaque type Element[F[_]] <: Node[F] = dom.Element
opaque type HtmlElement[F[_]] <: Element[F] = dom.HTMLElement
opaque type HtmlAnchorElement[F[_]] <: HtmlElement[F] = dom.HTMLAnchorElement
opaque type HtmlAreaElement[F[_]] <: HtmlElement[F] = dom.HTMLAreaElement
opaque type HtmlAudioElement[F[_]] <: HtmlElement[F] = dom.HTMLAudioElement
opaque type HtmlBaseElement[F[_]] <: HtmlElement[F] = dom.HTMLBaseElement
opaque type HtmlBodyElement[F[_]] <: HtmlElement[F] = dom.HTMLBodyElement

opaque type HtmlButtonElement[F[_]] <: HtmlElement[F] = dom.HTMLButtonElement
object HtmlButtonElement {
  extension [F[_]](button: HtmlButtonElement[F]) {
    def value(using Dom[F]): Ref[F, String] =
      new WrappedRef[F, String] {
        def unsafeGet() = button.value
        def unsafeSet(s: String) = button.value = s
      }
  }
}

opaque type HtmlBrElement[F[_]] <: HtmlElement[F] = dom.HTMLBRElement
opaque type HtmlCanvasElement[F[_]] <: HtmlElement[F] = dom.HTMLCanvasElement
opaque type HtmlDataListElement[F[_]] <: HtmlElement[F] = dom.HTMLDataListElement
opaque type HtmlDivElement[F[_]] <: HtmlElement[F] = dom.HTMLDivElement
opaque type HtmlDListElement[F[_]] <: HtmlElement[F] = dom.HTMLDListElement
opaque type HtmlEmbedElement[F[_]] <: HtmlElement[F] = dom.HTMLEmbedElement
opaque type HtmlFieldSetElement[F[_]] <: HtmlElement[F] = dom.HTMLFieldSetElement
opaque type HtmlFormElement[F[_]] <: HtmlElement[F] = dom.HTMLFormElement
opaque type HtmlHeadElement[F[_]] <: HtmlElement[F] = dom.HTMLHeadElement
opaque type HtmlHeadingElement[F[_]] <: HtmlElement[F] = dom.HTMLHeadingElement
opaque type HtmlHrElement[F[_]] <: HtmlElement[F] = dom.HTMLHRElement
opaque type HtmlHtmlElement[F[_]] <: HtmlElement[F] = dom.HTMLHtmlElement
opaque type HtmlIFrameElement[F[_]] <: HtmlElement[F] = dom.HTMLIFrameElement
opaque type HtmlImageElement[F[_]] <: HtmlElement[F] = dom.HTMLImageElement

opaque type HtmlInputElement[F[_]] <: HtmlElement[F] = dom.HTMLInputElement
object HtmlInputElement {
  extension [F[_]](input: HtmlInputElement[F]) {
    def checked(using Dom[F]): Ref[F, Boolean] =
      new WrappedRef[F, Boolean] {
        def unsafeGet() = input.checked
        def unsafeSet(b: Boolean) = input.checked = b
      }

    def value(using Dom[F]): Ref[F, String] =
      new WrappedRef[F, String] {
        def unsafeGet() = input.value
        def unsafeSet(s: String) = input.value = s
      }
  }
}

opaque type HtmlLinkElement[F[_]] <: HtmlElement[F] = dom.HTMLLinkElement
opaque type HtmlLabelElement[F[_]] <: HtmlElement[F] = dom.HTMLLabelElement
opaque type HtmlLegendElement[F[_]] <: HtmlElement[F] = dom.HTMLLegendElement
opaque type HtmlLiElement[F[_]] <: HtmlElement[F] = dom.HTMLLIElement
opaque type HtmlMapElement[F[_]] <: HtmlElement[F] = dom.HTMLMapElement
opaque type HtmlMenuElement[F[_]] <: HtmlElement[F] = dom.HTMLMenuElement
opaque type HtmlMetaElement[F[_]] <: HtmlElement[F] = dom.HTMLMetaElement
opaque type HtmlModElement[F[_]] <: HtmlElement[F] = dom.HTMLModElement
opaque type HtmlObjectElement[F[_]] <: HtmlElement[F] = dom.HTMLObjectElement
opaque type HtmlOListElement[F[_]] <: HtmlElement[F] = dom.HTMLOListElement
opaque type HtmlOptGroupElement[F[_]] <: HtmlElement[F] = dom.HTMLOptGroupElement

opaque type HtmlOptionElement[F[_]] <: HtmlElement[F] = dom.HTMLOptionElement
object HtmlOptionElement {
  extension [F[_]](option: HtmlOptionElement[F]) {
    def value(using Dom[F]): Ref[F, String] =
      new WrappedRef[F, String] {
        def unsafeGet() = option.value
        def unsafeSet(s: String) = option.value = s
      }
  }
}

opaque type HtmlParagraphElement[F[_]] <: HtmlElement[F] = dom.HTMLParagraphElement
opaque type HtmlParamElement[F[_]] <: HtmlElement[F] = dom.HTMLParamElement
opaque type HtmlPreElement[F[_]] <: HtmlElement[F] = dom.HTMLPreElement
opaque type HtmlProgressElement[F[_]] <: HtmlElement[F] = dom.HTMLProgressElement
opaque type HtmlQuoteElement[F[_]] <: HtmlElement[F] = dom.HTMLQuoteElement
opaque type HtmlScriptElement[F[_]] <: HtmlElement[F] = dom.HTMLScriptElement

opaque type HtmlSelectElement[F[_]] <: HtmlElement[F] = dom.HTMLSelectElement
object HtmlSelectElement {
  extension [F[_]](select: HtmlSelectElement[F]) {
    def value(using Dom[F]): Ref[F, String] =
      new WrappedRef[F, String] {
        def unsafeGet() = select.value
        def unsafeSet(s: String) = select.value = s
      }
  }
}

opaque type HtmlSourceElement[F[_]] <: HtmlElement[F] = dom.HTMLSourceElement
opaque type HtmlSpanElement[F[_]] <: HtmlElement[F] = dom.HTMLSpanElement
opaque type HtmlStyleElement[F[_]] <: HtmlElement[F] = dom.HTMLStyleElement
opaque type HtmlTableElement[F[_]] <: HtmlElement[F] = dom.HTMLTableElement
opaque type HtmlTableCaptionElement[F[_]] <: HtmlElement[F] = dom.HTMLTableCaptionElement
opaque type HtmlTableCellElement[F[_]] <: HtmlElement[F] = dom.HTMLTableCellElement
opaque type HtmlTableColElement[F[_]] <: HtmlElement[F] = dom.HTMLTableColElement
opaque type HtmlTableRowElement[F[_]] <: HtmlElement[F] = dom.HTMLTableRowElement
opaque type HtmlTableSectionElement[F[_]] <: HtmlElement[F] = dom.HTMLTableSectionElement

opaque type HtmlTextAreaElement[F[_]] <: HtmlElement[F] = dom.HTMLTextAreaElement
object HtmlTextAreaElement {
  extension [F[_]](textArea: HtmlTextAreaElement[F]) {
    def value(using Dom[F]): Ref[F, String] =
      new WrappedRef[F, String] {
        def unsafeGet() = textArea.value
        def unsafeSet(s: String) = textArea.value = s
      }
  }
}

opaque type HtmlTitleElement[F[_]] <: HtmlElement[F] = dom.HTMLTitleElement
opaque type HtmlTrackElement[F[_]] <: HtmlElement[F] = dom.HTMLTrackElement
opaque type HtmlUListElement[F[_]] <: HtmlElement[F] = dom.HTMLUListElement
opaque type HtmlVideoElement[F[_]] <: HtmlElement[F] = dom.HTMLVideoElement
