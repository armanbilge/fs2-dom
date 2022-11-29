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
import fs2.concurrent.Channel
import munit.CatsEffectSuite

import scala.concurrent.duration._
import scala.scalajs.concurrent.QueueExecutionContext

class HistorySuite extends CatsEffectSuite {

  test("history") {
    val history = History[IO, Int]
    Channel.unbounded[IO, Int].flatMap { ch =>
      history.state.discrete.unNone.through(ch.sendAll).compile.drain.background.surround {
        for {
          _ <- IO.sleep(1.second) // so popstate listener can register
          _ <- history.state.get.assertEquals(None)
          _ <- history.pushState(1)
          _ <- history.state.get.assertEquals(Some(1))
          _ <- history.pushState(2)
          _ <- history.state.get.assertEquals(Some(2))
          _ <- history.replaceState(3)
          _ <- history.state.get.assertEquals(Some(3))
          _ <- history.back
          _ <- history.state.get.assertEquals(Some(1))
          _ <- history.forward
          _ <- history.state.get.assertEquals(Some(3))
          _ <- ch.stream.take(2).compile.toList.assertEquals(List(1, 3))
        } yield ()
      }
    }.evalOn(QueueExecutionContext.promises())
  }

}
