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

trait Dom[F[_]] extends js.Any
object Dom {
  @inline implicit def forIO: Dom[IO] = IO.asyncForIO.asInstanceOf[Dom[IO]]
  @inline def forAsync[F[_]](implicit F: Async[F]): Dom[F] = F.asInstanceOf[Dom[F]]

  private[dom] implicit def toAsync[F[_]](dom: Dom[F]): Async[F] = dom.asInstanceOf[Async[F]]
  private[dom] implicit def domIsAsync[F[_]](implicit dom: Dom[F]): Async[F] =
    dom.asInstanceOf[Async[F]]
}

trait Node[F[_]] extends js.Any
object Node {

  implicit def ops[F[_]](node: Node[F]): Ops[F] = new Ops(node.asInstanceOf[dom.Node])

  private[dom] implicit def toJS[F[_]](node: Node[F]): dom.Node = node.asInstanceOf[dom.Node]
  private[dom] implicit def fromJS[F[_]](node: dom.Node): Node[F] = node.asInstanceOf[Node[F]]

  final class Ops[F[_]] private[Node] (private val node: dom.Node) extends AnyVal {

    def firstChild(implicit F: Dom[F]): F[Option[Node[F]]] =
      F.delay(Option(node.firstChild))

    def parentNode(implicit F: Dom[F]): F[Option[Node[F]]] =
      F.delay(Option(node.parentNode))

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

trait Document[F[_]] extends Node[F]
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
      F.delay(Option(document.getElementById(id)))

  }
}

trait HtmlDocument[F[_]] extends Document[F]

trait Element[F[_]] extends Node[F]
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

trait HtmlElement[F[_]] extends Element[F]

trait HtmlAnchorElement[F[_]] extends HtmlElement[F]
object HtmlAnchorElement {
  implicit def ops[F[_]](element: HtmlAnchorElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLAnchorElement])

  final class Ops[F[_]] private[HtmlAnchorElement] (private val anchor: dom.HTMLAnchorElement)
      extends AnyVal {
    def href(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => anchor.href, anchor.href = _)(F)
  }
}

trait HtmlAreaElement[F[_]] extends HtmlElement[F]
trait HtmlAudioElement[F[_]] extends HtmlElement[F]
trait HtmlBaseElement[F[_]] extends HtmlElement[F]
trait HtmlBodyElement[F[_]] extends HtmlElement[F]

trait HtmlButtonElement[F[_]] extends HtmlElement[F]
object HtmlButtonElement {
  implicit def ops[F[_]](element: HtmlButtonElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLButtonElement])

  final class Ops[F[_]] private[HtmlButtonElement] (private val button: dom.HTMLButtonElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => button.value, button.value = _)(F)
  }
}

trait HtmlBrElement[F[_]] extends HtmlElement[F]
trait HtmlCanvasElement[F[_]] extends HtmlElement[F]
trait HtmlDataListElement[F[_]] extends HtmlElement[F]
trait HtmlDivElement[F[_]] extends HtmlElement[F]
trait HtmlDListElement[F[_]] extends HtmlElement[F]
trait HtmlEmbedElement[F[_]] extends HtmlElement[F]
trait HtmlFieldSetElement[F[_]] extends HtmlElement[F]
trait HtmlFormElement[F[_]] extends HtmlElement[F]
trait HtmlHeadElement[F[_]] extends HtmlElement[F]
trait HtmlHeadingElement[F[_]] extends HtmlElement[F]
trait HtmlHrElement[F[_]] extends HtmlElement[F]
trait HtmlHtmlElement[F[_]] extends HtmlElement[F]
trait HtmlIFrameElement[F[_]] extends HtmlElement[F]
trait HtmlImageElement[F[_]] extends HtmlElement[F]

trait HtmlInputElement[F[_]] extends HtmlElement[F]
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

trait HtmlLinkElement[F[_]] extends HtmlElement[F]
trait HtmlLabelElement[F[_]] extends HtmlElement[F]
trait HtmlLegendElement[F[_]] extends HtmlElement[F]
trait HtmlLiElement[F[_]] extends HtmlElement[F]
trait HtmlMapElement[F[_]] extends HtmlElement[F]
trait HtmlMenuElement[F[_]] extends HtmlElement[F]
trait HtmlMetaElement[F[_]] extends HtmlElement[F]
trait HtmlModElement[F[_]] extends HtmlElement[F]
trait HtmlObjectElement[F[_]] extends HtmlElement[F]
trait HtmlOListElement[F[_]] extends HtmlElement[F]
trait HtmlOptGroupElement[F[_]] extends HtmlElement[F]

trait HtmlOptionElement[F[_]] extends HtmlElement[F]
object HtmlOptionElement {
  implicit def ops[F[_]](element: HtmlOptionElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLOptionElement])

  final class Ops[F[_]] private[HtmlOptionElement] (private val option: dom.HTMLOptionElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => option.value, option.value = _)(F)
  }
}

trait HtmlParagraphElement[F[_]] extends HtmlElement[F]
trait HtmlParamElement[F[_]] extends HtmlElement[F]
trait HtmlPreElement[F[_]] extends HtmlElement[F]
trait HtmlProgressElement[F[_]] extends HtmlElement[F]
trait HtmlQuoteElement[F[_]] extends HtmlElement[F]
trait HtmlScriptElement[F[_]] extends HtmlElement[F]

trait HtmlSelectElement[F[_]] extends HtmlElement[F]
object HtmlSelectElement {
  implicit def ops[F[_]](element: HtmlSelectElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLSelectElement])

  final class Ops[F[_]] private[HtmlSelectElement] (private val select: dom.HTMLSelectElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => select.value, select.value = _)(F)
  }
}

trait HtmlSourceElement[F[_]] extends HtmlElement[F]
trait HtmlSpanElement[F[_]] extends HtmlElement[F]
trait HtmlStyleElement[F[_]] extends HtmlElement[F]
trait HtmlTableElement[F[_]] extends HtmlElement[F]
trait HtmlTableCaptionElement[F[_]] extends HtmlElement[F]
trait HtmlTableCellElement[F[_]] extends HtmlElement[F]
trait HtmlTableColElement[F[_]] extends HtmlElement[F]
trait HtmlTableRowElement[F[_]] extends HtmlElement[F]
trait HtmlTableSectionElement[F[_]] extends HtmlElement[F]

trait HtmlTextAreaElement[F[_]] extends HtmlElement[F]
object HtmlTextAreaElement {
  implicit def ops[F[_]](element: HtmlTextAreaElement[F]): Ops[F] =
    new Ops(element.asInstanceOf[dom.HTMLTextAreaElement])

  final class Ops[F[_]] private[HtmlTextAreaElement] (private val textArea: dom.HTMLTextAreaElement)
      extends AnyVal {
    def value(implicit F: Dom[F]): Ref[F, String] =
      new WrappedRef[F, String](() => textArea.value, textArea.value = _)(F)
  }
}

trait HtmlTitleElement[F[_]] extends HtmlElement[F]
trait HtmlTrackElement[F[_]] extends HtmlElement[F]
trait HtmlUListElement[F[_]] extends HtmlElement[F]
trait HtmlVideoElement[F[_]] extends HtmlElement[F]
trait HtmlDialogElement[F[_]] extends HtmlElement[F]
