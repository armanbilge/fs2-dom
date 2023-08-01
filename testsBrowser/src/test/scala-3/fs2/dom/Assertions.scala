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

import scala.scalajs.js

object Assertions {

  import scala.reflect.ClassTag
  import scalajs.js.{JavaScriptException, ReferenceError}

  /** Assert whether the global referenced by `A` exists. */
  def assertGlobal[A <: js.Any](using loc: munit.Location, C: ClassTag[A]): Unit = {
    var x: js.Any = 1
    // this should get compiled to an instance check referencing the global object
    try
      x match {
        case _: A =>
          // If the check is erased, then we don't do an instance check
          munit.Assertions.fail("global is not bound correctly")
        case _ =>
      }
    catch {
      case e: JavaScriptException if e.exception.isInstanceOf[ReferenceError] =>
        // Global doesn't exist
        munit.Assertions.fail(s"could not find referenced global ${e}")
    }
  }
}
