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

package fs2.dom.facade

import org.scalajs.dom.AbortSignal

import scala.scalajs.js

@js.native
private[dom] trait LockManager extends js.Object {

  def request(
      name: String,
      options: LockRequestOptions,
      callback: js.Function1[Lock, js.Promise[Unit]]
  ): js.Promise[Unit] = js.native

}

private[dom] trait LockRequestOptions extends js.Object {

  var mode: js.UndefOr[String] = js.undefined

  var ifAvailable: js.UndefOr[Boolean] = js.undefined

  var signal: js.UndefOr[AbortSignal] = js.undefined

}

@js.native
private[dom] trait Lock extends js.Object
