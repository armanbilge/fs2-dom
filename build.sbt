ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)

ThisBuild / crossScalaVersions := Seq("3.2.0", "2.13.10")

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlJdkRelease := Some(8)

ThisBuild / githubWorkflowBuildMatrixAdditions +=
  "project" -> List("rootNodeJS", "rootChrome", "rootFirefox")
ThisBuild / githubWorkflowBuildSbtStepPreamble += s"project $${{ matrix.project }}"

val fs2Version = "3.3.0"
val sjsDomVersion = "2.3.0"
val munitCEVersion = "2.0.0-M3"

lazy val root = project.in(file(".")).aggregate(rootNodeJS, rootChrome, rootFirefox)

lazy val rootNodeJS =
  project.in(file(".rootNodeJS")).aggregate(dom, testsNodeJS)

lazy val rootChrome =
  project.in(file(".rootChrome")).aggregate(dom, testsChrome)

lazy val rootFirefox =
  project.in(file(".rootFirefox")).aggregate(dom, testsFirefox)

lazy val dom = project
  .in(file("dom"))
  .settings(
    name := "fs2-dom",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % fs2Version,
      "org.scala-js" %%% "scala-js-dom" % sjsDomVersion
    )
  )

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.scalajs.jsenv.JSEnv
import org.scalajs.jsenv.nodejs.NodeJSEnv
import org.scalajs.jsenv.selenium.SeleniumJSEnv

def configureTest(project: Project): Project =
  project
    .dependsOn(dom)
    .enablePlugins(ScalaJSPlugin, NoPublishPlugin)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit-cats-effect" % munitCEVersion
      ),
      Compile / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "main" / "scala",
      Test / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "test" / "scala"
    )

lazy val testsNodeJS = project
  .configure(configureTest)

lazy val testsChrome = project
  .configure(configureTest)
  .settings(
    jsEnv := {
      val config = SeleniumJSEnv.Config()
      val options = new ChromeOptions()
      options.setHeadless(true)
      new SeleniumJSEnv(options, config)
    }
  )

lazy val testsFirefox = project
  .configure(configureTest)
  .settings(
    jsEnv := {
      val config = SeleniumJSEnv.Config()
      val options = new FirefoxOptions()
      options.setHeadless(true)
      new SeleniumJSEnv(options, config)
    }
  )
