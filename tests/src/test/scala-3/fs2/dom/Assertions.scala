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

import org.scalajs.dom
import scala.scalajs.js
import js.annotation.*
import scala.quoted.*

object Assertions {
  private def assertGlobalImpl[A <: js.Any](
      loc: Expr[munit.Location]
  )(using Type[A], Quotes): Expr[Unit] = {
    import quotes.reflect.*
    val pkg = TypeRepr.of[dom.Element].typeSymbol.owner
    val typ = TypeRepr.of[A]
    val global = typ.typeSymbol.annotations
      .map(_.asExpr)
      .collect {
        case expr if expr.isExprOf[JSGlobal] =>
          expr.asTerm match {
            case Apply(_, Literal(StringConstant(s)) :: Nil) => s
            case _                                           => typ.typeSymbol.name.toString
          }
      }
      .headOption
    global match {
      case Some(global) =>
        pkg.declaredType(global).headOption match {
          case None =>
            val err = Literal(StringConstant(s"'${global}' does not exist in '${pkg.fullName}'"))
            '{ munit.Assertions.fail(${ err.asExprOf[String] })(using $loc) }
          case _ =>
            // All good, return unit
            '{ () }
        }
      case None =>
        report.errorAndAbort(s"unable to find 'JSGlobal' annotation on ${Type.show[A]}")
    }
  }

  /** Assert that a similar 'JSGlobal' exists in 'org.scalajs.dom'.
    *
    * Assumes that 'org.scalajs.dom' has an exact name match.
    */
  inline def assertGlobal[A <: js.Any](using loc: munit.Location): Unit = ${
    assertGlobalImpl[A]('loc)
  }
}
