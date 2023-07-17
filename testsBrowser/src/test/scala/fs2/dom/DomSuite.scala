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
import munit.CatsEffectSuite

class DomSuite extends CatsEffectSuite {
  val window = Window[IO]

  test("asInstanceOf") {
    Document.ops(window.document).createElement("button").map {
      case _: HtmlInputElement[IO] => false
      case _: HtmlButtonElement[IO] => true
      case _ => false
    }.assert
  }

}
