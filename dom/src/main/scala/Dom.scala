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
import org.scalajs.dom

import scala.scalajs.js
import js.annotation._

sealed trait Dom[F[_]] extends js.Any
object Dom {
  @inline implicit def forIO: Dom[IO] = IO.asyncForIO.asInstanceOf[Dom[IO]]
  @inline def forAsync[F[_]](implicit F: Async[F]): Dom[F] = F.asInstanceOf[Dom[F]]

  private[dom] implicit def toAsync[F[_]](dom: Dom[F]): Async[F] = dom.asInstanceOf[Async[F]]
  private[dom] implicit def domIsAsync[F[_]](implicit dom: Dom[F]): Async[F] =
    dom.asInstanceOf[Async[F]]
}

@JSGlobal
@js.native
class Node[F[_]] protected () extends js.Any
object Node {

  implicit def ops[F[_]](node: Node[F]): Ops[F] = new Ops(node.asInstanceOf[dom.Node])

  private[dom] implicit def toJS[F[_]](node: Node[F]): dom.Node = node.asInstanceOf[dom.Node]
  private[dom] implicit def fromJS[F[_]](node: dom.Node): Node[F] = node.asInstanceOf[Node[F]]

  final class Ops[F[_]] private[Node] (private val node: dom.Node) extends AnyVal {

    def firstChild(implicit F: Dom[F]): F[Option[Node[F]]] =
      F.delay(Option(fromJS(node.firstChild)))

    def parentNode(implicit F: Dom[F]): F[Option[Node[F]]] =
      F.delay(Option(fromJS(node.parentNode)))

    def appendChild(child: Node[F])(implicit F: Dom[F]): F[Unit] = F.delay {
      node.appendChild(child)
      ()
    }

    def removeChild(child: Node[F])(implicit F: Dom[F]): F[Unit] = F.delay {
      node.removeChild(child)
      ()
    }

    def replaceChild(newChild: Node[F], oldChild: Node[F])(implicit F: Dom[F]): F[Unit] = F.delay {
      node.replaceChild(newChild, oldChild)
      ()
    }
  }
}

@JSGlobal
@js.native
class Document[F[_]] protected () extends Node[F]
object Document {

  implicit def ops[F[_]](document: Document[F]): Ops[F] = new Ops(
    document.asInstanceOf[dom.Document]
  )

  private[dom] implicit def toJS[F[_]](document: Document[F]): dom.Document =
    document.asInstanceOf[dom.Document]
  private[dom] implicit def fromJS[F[_]](document: dom.Document): Document[F] =
    document.asInstanceOf[Document[F]]

  final class Ops[F[_]] private[Document] (private val document: dom.Document) extends AnyVal {

    def createElement(tagName: String)(implicit F: Dom[F]): F[Element[F]] =
      F.delay(document.createElement(tagName))

    def getElementById(id: String)(implicit F: Dom[F]): F[Option[Element[F]]] =
      F.delay(Option(Element.fromJS(document.getElementById(id))))

  }
}

@JSGlobal("HTMLDocument")
@js.native
class HtmlDocument[F[_]] protected () extends Document[F]
object HtmlDocument {

  implicit def ops[F[_]](element: HtmlDocument[F]): Ops[F] = new Ops(
    element.asInstanceOf[dom.HTMLDocument]
  )

  private implicit def toJS[F[_]](element: HtmlDocument[F]): dom.HTMLDocument =
    element.asInstanceOf[dom.HTMLDocument]
  private[dom] implicit def fromJS[F[_]](element: dom.HTMLDocument): HtmlDocument[F] =
    element.asInstanceOf[HtmlDocument[F]]

  final class Ops[F[_]] private[HtmlDocument] (private val document: HtmlDocument[F])
      extends AnyVal {
    def readyState(implicit F: Dom[F]): F[dom.DocumentReadyState] =
      F.delay(toJS(document).readyState)
  }
}

@JSGlobal
@js.native
class Element[F[_]] protected () extends Node[F]
object Element {
  implicit def ops[F[_]](element: Element[F]): Ops[F] = new Ops(element.asInstanceOf[dom.Element])

  private[dom] implicit def toJS[F[_]](element: Element[F]): dom.Element =
    element.asInstanceOf[dom.Element]
  private[dom] implicit def fromJS[F[_]](element: dom.Element): Element[F] =
    element.asInstanceOf[Element[F]]

  final class Ops[F[_]] private[Element] (private val element: Element[F]) extends AnyVal {
    def children(implicit F: Dom[F]): HtmlCollection[F, Element[F]] =
      HtmlCollection((element: dom.Element).children.asInstanceOf[dom.HTMLCollection[Element[F]]])(
        F
      )

    def innerHtml(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => element.innerHTML, element.innerHTML = _)(F)
  }
}

@JSGlobal("HTMLElement")
@js.native
class HtmlElement[F[_]] protected () extends Element[F]
object HtmlElement {

  implicit def ops[F[_]](element: HtmlElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLElement])

  final class Ops[F[_]] private[HtmlElement] (private val element: dom.HTMLElement) extends AnyVal {
    def focus(implicit F: Dom[F]): F[Unit] = F.delay(element.focus())

    def focus(options: dom.FocusOptions)(implicit F: Dom[F]): F[Unit] =
      F.delay(element.focus(options))

    def blur(implicit F: Dom[F]): F[Unit] = F.delay(element.blur())

    def click(implicit F: Dom[F]): F[Unit] = F.delay(element.click())

    def offsetHeight(implicit F: Dom[F]): F[Int] = F.delay(element.offsetHeight.toInt)

    def offsetWidth(implicit F: Dom[F]): F[Int] = F.delay(element.offsetWidth.toInt)

    def offsetParent(implicit F: Dom[F]): F[Option[Element[F]]] =
      F.delay(Option(Element.fromJS(element.offsetParent)))

    def offsetTop(implicit F: Dom[F]): F[Int] = F.delay(element.offsetTop.toInt)

    def offsetLeft(implicit F: Dom[F]): F[Int] = F.delay(element.offsetLeft.toInt)

    def isContentEditable(implicit F: Dom[F]): F[Boolean] = F.delay(element.isContentEditable)
  }

}

@JSGlobal("HTMLAnchorElement")
@js.native
class HtmlAnchorElement[F[_]] protected () extends HtmlElement[F]
object HtmlAnchorElement {
  implicit def ops[F[_]](element: HtmlAnchorElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLAnchorElement])

  final class Ops[F[_]] private[HtmlAnchorElement] (private val anchor: dom.HTMLAnchorElement)
      extends AnyVal {
    def href(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => anchor.href, anchor.href = _)(F)
  }
}

@JSGlobal("HTMLAreaElement")
@js.native
class HtmlAreaElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLAudioElement")
@js.native
class HtmlAudioElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLBaseElement")
@js.native
class HtmlBaseElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLBodyElement")
@js.native
class HtmlBodyElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLButtonElement")
@js.native
class HtmlButtonElement[F[_]] protected () extends HtmlElement[F]
object HtmlButtonElement {
  implicit def ops[F[_]](element: HtmlButtonElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLButtonElement])

  final class Ops[F[_]] private[HtmlButtonElement] (private val button: dom.HTMLButtonElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => button.value, button.value = _)(F)
  }
}

@JSGlobal("HTMLBRElement")
@js.native
class HtmlBrElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLCanvasElement")
@js.native
class HtmlCanvasElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLDataListElement")
@js.native
class HtmlDataListElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLDivElement")
@js.native
class HtmlDivElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLDListElement")
@js.native
class HtmlDListElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLEmbedElement")
@js.native
class HtmlEmbedElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLFieldSetElement")
@js.native
class HtmlFieldSetElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLFormElement")
@js.native
class HtmlFormElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLHeadElement")
@js.native
class HtmlHeadElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLHeadingElement")
@js.native
class HtmlHeadingElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLHRElement")
@js.native
class HtmlHrElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLHtmlElement")
@js.native
class HtmlHtmlElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLIFrameElement")
@js.native
class HtmlIFrameElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLImageElement")
@js.native
class HtmlImageElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLInputElement")
@js.native
class HtmlInputElement[F[_]] protected () extends HtmlElement[F]
object HtmlInputElement {
  implicit def ops[F[_]](element: HtmlInputElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLInputElement])

  final class Ops[F[_]] private[HtmlInputElement] (private val input: dom.HTMLInputElement)
      extends AnyVal {
    def checked(implicit F: Dom[F]): Ref[F, Boolean] =
      new WrappedRef[F, Boolean](() => input.checked, input.checked = _)(F)

    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => input.value, input.value = _)(F)
  }
}

@JSGlobal("HTMLLinkElement")
@js.native
class HtmlLinkElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLLabelElement")
@js.native
class HtmlLabelElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLLegendElement")
@js.native
class HtmlLegendElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLLIElement")
@js.native
class HtmlLiElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLMapElement")
@js.native
class HtmlMapElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLMenuElement")
@js.native
class HtmlMenuElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLMetaElement")
@js.native
class HtmlMetaElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLModElement")
@js.native
class HtmlModElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLObjectElement")
@js.native
class HtmlObjectElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLOListElement")
@js.native
class HtmlOListElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLOptGroupElement")
@js.native
class HtmlOptGroupElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLOptionElement")
@js.native
class HtmlOptionElement[F[_]] protected () extends HtmlElement[F]
object HtmlOptionElement {
  implicit def ops[F[_]](element: HtmlOptionElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLOptionElement])

  final class Ops[F[_]] private[HtmlOptionElement] (private val option: dom.HTMLOptionElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => option.value, option.value = _)(F)
  }
}

@JSGlobal("HTMLParagraphElement")
@js.native
class HtmlParagraphElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLParamElement")
@js.native
class HtmlParamElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLPreElement")
@js.native
class HtmlPreElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLProgressElement")
@js.native
class HtmlProgressElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLQuoteElement")
@js.native
class HtmlQuoteElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLScriptElement")
@js.native
class HtmlScriptElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLSelectElement")
@js.native
class HtmlSelectElement[F[_]] protected () extends HtmlElement[F]
object HtmlSelectElement {
  implicit def ops[F[_]](element: HtmlSelectElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLSelectElement])

  final class Ops[F[_]] private[HtmlSelectElement] (private val select: dom.HTMLSelectElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => select.value, select.value = _)(F)
  }
}

@JSGlobal("HTMLSourceElement")
@js.native
class HtmlSourceElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLSpanElement")
@js.native
class HtmlSpanElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLStyleElement")
@js.native
class HtmlStyleElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableElement")
@js.native
class HtmlTableElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableCaptionElement")
@js.native
class HtmlTableCaptionElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableCellElement")
@js.native
class HtmlTableCellElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableColElement")
@js.native
class HtmlTableColElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableRowElement")
@js.native
class HtmlTableRowElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTableSectionElement")
@js.native
class HtmlTableSectionElement[F[_]] protected () extends HtmlElement[F]

@JSGlobal("HTMLTextAreaElement")
@js.native
class HtmlTextAreaElement[F[_]] protected () extends HtmlElement[F]
object HtmlTextAreaElement {
  implicit def ops[F[_]](element: HtmlTextAreaElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLTextAreaElement])

  final class Ops[F[_]] private[HtmlTextAreaElement] (private val textArea: dom.HTMLTextAreaElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => textArea.value, textArea.value = _)(F)
  }
}

@JSGlobal("HTMLTitleElement")
@js.native
class HtmlTitleElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLTrackElement")
@js.native
class HtmlTrackElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLUListElement")
@js.native
class HtmlUListElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLVideoElement")
@js.native
class HtmlVideoElement[F[_]] protected () extends HtmlElement[F]
@JSGlobal("HTMLDialogElement")
@js.native
class HtmlDialogElement[F[_]] protected () extends HtmlElement[F]
