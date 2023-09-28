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

import scala.concurrent.duration._

class BroadcastChannelSuite extends CatsEffectSuite {

  implicit def intSerializer: Serializer[Int] =
    Serializer.any.imap(_.asInstanceOf[Int])(identity(_))

  test("BroadcastChannel") {
    val channel = BroadcastChannel[IO, Int]("integer channel")
    val send = channel.use(_.postMessage(42))
    val receive = channel.use(_.messages.head.compile.onlyOrError).map(_.data)

    (IO.sleep(1.second) *> send).background.surround(receive).assertEquals(42)
  }

}
