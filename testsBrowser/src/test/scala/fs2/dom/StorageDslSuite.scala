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
import cats.effect.std.Random
import munit.CatsEffectSuite

abstract class StorageDslSuite(storageType: StorageDsl.Type) extends CatsEffectSuite {

  test("StorageDsl[IO]") {
    val run = for {
      random <- Random.scalaUtilRandom[IO]
      key <- random.nextString(8)
      value <- random.nextString(128)

      _ <- StorageDsl[IO].length(storageType).assertEquals(0)
      _ <- StorageDsl[IO].key(storageType, 0).assertEquals(None)
      _ <- StorageDsl[IO].getItem(storageType, key).assertEquals(None)

      _ <- StorageDsl[IO].setItem(storageType, key, value)
      _ <- StorageDsl[IO].length(storageType).assertEquals(1)
      _ <- StorageDsl[IO].key(storageType, 0).assertEquals(Some(key))
      _ <- StorageDsl[IO].getItem(storageType, key).assertEquals(Some(value))

      _ <- StorageDsl[IO].removeItem(storageType, key)
      _ <- StorageDsl[IO].length(storageType).assertEquals(0)
      _ <- StorageDsl[IO].key(storageType, 0).assertEquals(None)
      _ <- StorageDsl[IO].getItem(storageType, key).assertEquals(None)

    } yield ()

    run.guarantee(StorageDsl[IO].clear(storageType))
  }

}

class LocalStorageSuite extends StorageDslSuite(StorageDsl.Type.Local)

class SessionStorageSuite extends StorageDslSuite(StorageDsl.Type.Session)
