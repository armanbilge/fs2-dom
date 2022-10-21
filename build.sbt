ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2022)
ThisBuild / tlSonatypeUseLegacyHost := false

ThisBuild / crossScalaVersions := Seq("3.2.0", "2.13.10")

ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))
ThisBuild / tlJdkRelease := Some(8)

ThisBuild / githubWorkflowBuildMatrixAdditions +=
  "project" -> List("rootNodeJS", "rootChrome", "rootFirefox")
ThisBuild / githubWorkflowBuildSbtStepPreamble += s"project $${{ matrix.project }}"

ThisBuild / githubWorkflowBuildPreamble +=
  WorkflowStep.Use(
    UseRef.Public("actions", "setup-node", "v3"),
    name = Some("Setup Node.js"),
    params = Map("node-version" -> "18"),
    cond = Some("matrix.project == 'rootNodeJS'")
  )

val ceVersion = "3.4.0-RC2"
val fs2Version = "3.3.0"
val circeVersion = "0.14.3"
val sjsDomVersion = "2.3.0"
val weaverVersion = "0.8.0"

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
      "org.scala-js" %%% "scalajs-dom" % sjsDomVersion,
      "io.circe" %%% "circe-scalajs" % circeVersion
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
        "com.disneystreaming" %%% "weaver-cats" % weaverVersion,
        "com.disneystreaming" %%% "weaver-scalacheck" % weaverVersion
      ),
      Compile / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "main" / "scala",
      Test / unmanagedSourceDirectories +=
        (LocalRootProject / baseDirectory).value / "tests" / "src" / "test" / "scala",
      testFrameworks += new TestFramework("weaver.framework.CatsEffect")
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
