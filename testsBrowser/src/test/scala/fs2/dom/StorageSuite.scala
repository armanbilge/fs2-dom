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
import cats.effect.std.Random

abstract class StorageSuite(storage: Storage[IO]) extends CatsEffectSuite {

  test("storage") {
    val run = for {
      random <- Random.scalaUtilRandom[IO]
      key <- random.nextString(8)
      value <- random.nextString(128)

      _ <- storage.length.assertEquals(0)
      _ <- storage.key(0).assertEquals(None)
      _ <- storage.getItem(key).assertEquals(None)

      _ <- storage.setItem(key, value)
      _ <- storage.length.assertEquals(1)
      _ <- storage.key(0).assertEquals(Some(key))
      _ <- storage.getItem(key).assertEquals(Some(value))

      _ <- storage.removeItem(key)
      _ <- storage.length.assertEquals(0)
      _ <- storage.key(0).assertEquals(None)
      _ <- storage.getItem(key).assertEquals(None)

    } yield ()

    run.guarantee(storage.clear)
  }

}

class LocalStorageSuite extends StorageSuite(Storage.local)

class SessionStorageSuite extends StorageSuite(Storage.session)
