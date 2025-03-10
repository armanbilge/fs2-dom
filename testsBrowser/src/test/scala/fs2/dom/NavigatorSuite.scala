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

class NavigatorSuite extends CatsEffectSuite {

  val navigater = Window[IO].navigator

  test("browser connectivity") {
    navigater.onLine.assertEquals(true)

  }

  test("bowser language") {
    navigater.language.assertEquals("en-US")
  }

  test("browser languages") {
    for {
      languages <- navigater.languages
      _ <- IO {
        assert(languages.contains("en-US"))
      }
    } yield ()
  }

}
