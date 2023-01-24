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

class LockManagerSuite extends CatsEffectSuite {

  def locks = Window[IO].navigator.locks

  test("no contention") {
    IO.ref(false).flatMap { ref =>
      locks.exclusive("lock").surround(ref.set(true)) *> ref.get.assert
    }
  }

  test("use cancelable") {
    val cancel = locks.exclusive("lock").surround(IO.never).timeoutTo(1.second, IO.unit)
    val reacquire = IO.ref(false).flatMap { ref =>
      locks.exclusive("lock").surround(ref.set(true)) *> ref.get.assert
    }

    cancel *> reacquire
  }

  test("exclusivity") {
    locks.exclusive("lock").surround(IO.never).start.flatMap { f =>
      IO.sleep(1.second) *>
        locks.tryExclusive("lock").use(IO.pure(_)).map(!_).assert *>
        f.cancel *>
        locks.tryExclusive("lock").use(IO.pure(_)).assert
    }
  }

  test("acquire cancelability") {
    locks.exclusive("lock").surround {
      IO.ref(true).flatMap { ref =>
        locks
          .exclusive("lock")
          .surround(ref.set(false))
          .as(false)
          .timeoutTo(1.second, IO(true))
          .assert *> ref.get.assert // assert it timed out before running
      }

    }
  }

}
