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

import cats.data.State
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.std.MapRef
import munit.CatsEffectSuite
import org.scalajs.dom.Storage
import org.scalajs.dom.window

abstract class StorageMapRefSuite extends CatsEffectSuite {

  def key = getClass.getName

  def storage: Storage

  def storageMapRef: Resource[IO, MapRef[IO, String, Option[String]]] =
    Resource.make(IO(StorageMapRef[IO](storage)))(_ => IO(storage.clear()))

  def ref = storageMapRef.map(_(key))

  test("get and set successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        getAndSetResult <- r.getAndSet(Some("1"))
        getResult <- r.get
        _ <- IO(assertEquals(getAndSetResult, Some("0")))
        _ <- IO(assertEquals(getResult, Some("1")))
      } yield ()
    }
  }

  test("get and update successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        getAndUpdateResult <- r.getAndUpdate(_.map(_ + "1"))
        getResult <- r.get
        _ <- IO(assertEquals(getAndUpdateResult, Some("0")))
        _ <- IO(assertEquals(getResult, Some("01")))
      } yield ()
    }
  }

  test("update and get successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        updateAndGetResult <- r.updateAndGet(_.map(_ + "1"))
        getResult <- r.get
        _ <- IO(assertEquals(updateAndGetResult, Some("01")))
        _ <- IO(assertEquals(getResult, Some("01")))
      } yield ()
    }
  }

  test("access successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        valueAndSetter <- r.access
        (value, setter) = valueAndSetter
        success <- setter(value.map(_ + "1"))
        result <- r.get
        _ <- IO(assert(success))
        _ <- IO(assertEquals(result, Some("01")))
      } yield ()
    }
  }

  test("access - setter should fail if value is modified before setter is called") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        valueAndSetter <- r.access
        (value, setter) = valueAndSetter
        _ <- r.set(Some("5"))
        success <- setter(value.map(_ + "1"))
        result <- r.get
        _ <- IO(assert(!success))
        _ <- IO(assertEquals(result, Some("5")))
      } yield ()
    }
  }

  test("tryUpdate - modification occurs successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        result <- r.tryUpdate(_.map(_ + "1"))
        getResult <- r.get
        _ <- IO(assert(result))
        _ <- IO(assertEquals(getResult, Some("01")))
      } yield ()
    }
  }

  test("tryUpdate - should fail to update if modification has occurred") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        result <- r.tryUpdate { currentValue =>
          storage.setItem(key, currentValue.map(_ + "1").orNull)
          currentValue.map(_ + "1")
        }
        _ <- IO(assert(result))
      } yield result
    }
  }

  test("tryModifyState - modification occurs successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        result <- r.tryModifyState(State.pure(Some("1")))
        _ <- IO(assert(clue(result).contains(Some("1"))))
      } yield ()
    }
  }

  test("modifyState - modification occurs successfully") {
    ref.use { r =>
      for {
        _ <- r.set(Some("0"))
        result <- r.modifyState(State.pure(Some("1")))
        _ <- IO(assertEquals(result, Some("1")))
      } yield ()
    }
  }

}

class LocalStorageMapRefSuite extends StorageMapRefSuite {
  def storage = window.localStorage
}

class SessionStorageMapRefSuite extends StorageMapRefSuite {
  def storage = window.sessionStorage
}
