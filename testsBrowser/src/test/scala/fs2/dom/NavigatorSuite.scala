package fs2.dom

import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration.DurationInt

class NavigatorSuite extends CatsEffectSuite {
  val navigater = Window[IO].navigator

  test("browser connectivity") {
    navigater.onLine.assertEquals(true)
  }

}
