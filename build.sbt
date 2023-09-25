ThisBuild / tlBaseVersion := "0.3"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

val scala213 = "2.13.11"
ThisBuild / crossScalaVersions := Seq(scala213, "3.3.1")
ThisBuild / scalaVersion := scala213

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

ThisBuild / githubWorkflowBuildMatrixAdditions +=
  "project" -> List("rootChrome", "rootFirefox", "rootNodeJS")
ThisBuild / githubWorkflowBuildSbtStepPreamble += s"project $${{ matrix.project }}"

ThisBuild / githubWorkflowBuildPreamble +=
  WorkflowStep.Use(
    UseRef.Public("actions", "setup-node", "v3"),
    name = Some("Setup Node.js"),
    params = Map("node-version" -> "18"),
    cond = Some("matrix.project == 'rootNodeJS'")
  )

val ceVersion = "3.5.1"
val fs2Version = "3.9.2"
val sjsDomVersion = "2.7.0"
val munitCEVersion = "2.0.0-M3"
val scalaCheckEffectVersion = "2.0.0-M2"

lazy val root = project
  .in(file("."))
  .aggregate(rootNodeJS, rootChrome, rootFirefox)
  .enablePlugins(NoPublishPlugin)

lazy val rootNodeJS =
  project.in(file(".rootNodeJS")).aggregate(dom, testsNodeJS).enablePlugins(NoPublishPlugin)

lazy val rootChrome =
  project.in(file(".rootChrome")).aggregate(dom, testsChrome).enablePlugins(NoPublishPlugin)

lazy val rootFirefox =
  project.in(file(".rootFirefox")).aggregate(dom, testsFirefox).enablePlugins(NoPublishPlugin)

lazy val dom = project
  .in(file("dom"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "fs2-dom",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % ceVersion,
      "co.fs2" %%% "fs2-core" % fs2Version,
      "org.scala-js" %%% "scalajs-dom" % sjsDomVersion
    ),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core.ProblemFilters._
      import com.typesafe.tools.mima.core._
      Seq(
        ProblemFilters.exclude[ReversedMissingMethodProblem]("fs2.dom.Window.domContentLoaded")
      )
    }
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
        "org.typelevel" %%% "munit-cats-effect" % munitCEVersion,
        "org.typelevel" %%% "scalacheck-effect-munit" % scalaCheckEffectVersion
      ),
      Compile / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "main" / "scala",
      Test / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "test" / "scala",
      testOptions += Tests.Argument("+l")
    )

def configureBrowserTest(project: Project): Project =
  project.settings(
    Compile / unmanagedSourceDirectories +=
      (LocalRootProject / baseDirectory).value / "testsBrowser" / "src" / "main" / "scala",
    Test / unmanagedSourceDirectories +=
      (LocalRootProject / baseDirectory).value / "testsBrowser" / "src" / "test" / "scala"
  )

lazy val testsNodeJS = project
  .configure(configureTest)

lazy val testsChrome = project
  .configure(configureTest, configureBrowserTest)
  .settings(
    jsEnv := {
      val config = SeleniumJSEnv.Config()
      val options = new ChromeOptions()
      options.setHeadless(true)
      new SeleniumJSEnv(options, config)
    }
  )

lazy val testsFirefox = project
  .configure(configureTest, configureBrowserTest)
  .settings(
    jsEnv := {
      val config = SeleniumJSEnv.Config()
      val options = new FirefoxOptions()
      options.setHeadless(true)
      new SeleniumJSEnv(options, config)
    }
  )
