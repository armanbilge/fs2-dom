package fs2.dom

import scala.reflect.ClassTag
import scala.scalajs.js
import Assertions.*

class GlobalSuite extends munit.FunSuite {

  inline transparent def testGlobal[F[_[_]] <: js.Any](using C: ClassTag[F[Nothing]]) =
    test(s"JSGlobal ${C.runtimeClass.getName}") {
      assertGlobal[F[Nothing]]
    }

  testGlobal[Node]
  testGlobal[Document]
  testGlobal[Element]
  testGlobal[HtmlElement]
  testGlobal[HtmlAnchorElement]
  testGlobal[HtmlAreaElement]
  testGlobal[HtmlAudioElement]
  testGlobal[HtmlBaseElement]
  testGlobal[HtmlBodyElement]
  testGlobal[HtmlButtonElement]
  testGlobal[HtmlBrElement]
  testGlobal[HtmlCanvasElement]
  testGlobal[HtmlDataListElement]
  testGlobal[HtmlDivElement]
  testGlobal[HtmlDListElement]
  testGlobal[HtmlEmbedElement]
  testGlobal[HtmlFieldSetElement]
  testGlobal[HtmlFormElement]
  testGlobal[HtmlHeadElement]
  testGlobal[HtmlHeadingElement]
  testGlobal[HtmlHrElement]
  testGlobal[HtmlHtmlElement]
  testGlobal[HtmlIFrameElement]
  testGlobal[HtmlImageElement]
  testGlobal[HtmlInputElement]
  testGlobal[HtmlLinkElement]
  testGlobal[HtmlLabelElement]
  testGlobal[HtmlLegendElement]
  testGlobal[HtmlLiElement]
  testGlobal[HtmlMapElement]
  testGlobal[HtmlMenuElement]
  testGlobal[HtmlMetaElement]
  testGlobal[HtmlModElement]
  testGlobal[HtmlObjectElement]
  testGlobal[HtmlOListElement]
  testGlobal[HtmlOptGroupElement]
  testGlobal[HtmlOptionElement]
  testGlobal[HtmlParagraphElement]
  testGlobal[HtmlParamElement]
  testGlobal[HtmlPreElement]
  testGlobal[HtmlProgressElement]
  testGlobal[HtmlQuoteElement]
  testGlobal[HtmlScriptElement]
  testGlobal[HtmlSelectElement]
  testGlobal[HtmlSourceElement]
  testGlobal[HtmlSpanElement]
  testGlobal[HtmlStyleElement]
  testGlobal[HtmlTableElement]
  testGlobal[HtmlTableCaptionElement]
  testGlobal[HtmlTableCellElement]
  testGlobal[HtmlTableColElement]
  testGlobal[HtmlTableRowElement]
  testGlobal[HtmlTableSectionElement]
  testGlobal[HtmlTitleElement]
  testGlobal[HtmlTrackElement]
  testGlobal[HtmlUListElement]
  testGlobal[HtmlVideoElement]
  testGlobal[HtmlDialogElement]

}
