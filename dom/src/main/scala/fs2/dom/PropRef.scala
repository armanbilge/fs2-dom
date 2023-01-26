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

import cats.effect.kernel.Sync

import scala.scalajs.js

private[dom] final class PropRef[F[_], A](obj: Any, prop: String)(implicit F: Sync[F])
    extends WrappedRef[F, A] {

  def unsafeGet(): A =
    obj.asInstanceOf[js.Dynamic].selectDynamic(prop).asInstanceOf[A]

  def unsafeSet(a: A): Unit =
    obj.asInstanceOf[js.Dynamic].updateDynamic(prop)(a.asInstanceOf[js.Any])

}
