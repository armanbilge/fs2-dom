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
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object StreamConversionSuite extends SimpleIOSuite with Checkers {

  test("to/read ReadableStream") {
    forall { (chunks: Vector[Vector[Byte]]) =>
      Stream
        .emits(chunks)
        .map(Chunk.seq(_))
        .unchunks
        .through(toReadableStream[IO])
        .flatMap(readable => readReadableStream(IO(readable)))
        .compile
        .toVector
        .map(expect.eql(_, chunks.flatten.drop(1)))
    }
  }

}
