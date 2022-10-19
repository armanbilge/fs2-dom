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

package fs2
package dom

import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF.forAllF

class StreamConversionSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("to/read Readable") {
    forAllF { (chunks: Vector[Vector[Byte]]) =>
      Stream
        .emits(chunks)
        .map(Chunk.seq(_))
        .unchunks
        .through(toReadableStream[IO])
        .flatMap(readable => readReadableStream(IO(readable)))
        .compile
        .toVector
        .assertEquals(chunks.flatten)
    }
  }

}
