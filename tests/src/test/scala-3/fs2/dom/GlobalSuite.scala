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
