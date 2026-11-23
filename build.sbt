Global / onChangedBuildSource := ReloadOnSourceChanges
Global / resolvers += "Sonatype Central Snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"

ThisBuild / tlBaseVersion := "0.1"

lazy val scala213 = "2.13.18"
lazy val scala3 = "3.10.1"
ThisBuild / scalaVersion := scala3
ThisBuild / crossScalaVersions := Seq(scala213, scala3)

ThisBuild / organization := "io.github.buntec"
ThisBuild / organizationName := "buntec"
ThisBuild / startYear := Some(2024)
ThisBuild / licenses := Seq(License.MIT, License.Apache2)
ThisBuild / tlCiHeaderCheck := false
ThisBuild / tlCiDependencyGraphJob := false

ThisBuild / developers := List(tlGitHubDev("buntec", "Christoph Bunte"))

ThisBuild / tlFatalWarnings := false
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

// the tests run in jsdom, and the icons are generated from the heroicons
// package; both are installed via npm (Scala.js jobs only)
lazy val jsOnly = Some("matrix.project == 'rootJS'")
ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Use(
    UseRef.Public("actions", "setup-node", "v6"),
    name = Some("Setup Node.js"),
    params = Map("node-version" -> "24", "cache" -> "npm"),
    cond = jsOnly
  ),
  WorkflowStep.Run(
    List("npm ci"),
    name = Some("Install npm dependencies"),
    cond = jsOnly
  )
)

// the committed icons must match what the generator produces from the
// heroicons version in package-lock.json (checked once, on Scala 3)
ThisBuild / githubWorkflowBuild += WorkflowStep.Run(
  List(
    "sbt generateHeroicons",
    "git diff --exit-code -- ff4s-heroicons/src/main/scala"
  ),
  name = Some("Check that generated icons are up to date"),
  cond = Some("matrix.project == 'rootJS' && matrix.scala == '3'")
)

lazy val generateHeroicons =
  taskKey[Unit]("generates the component definitions")

lazy val ff4sVersion = "0.27.0"
lazy val scalajsDomVersion = "2.8.1"
lazy val catsVersion = "2.13.0"
lazy val catsEffectVersion = "3.7.1"
lazy val fs2Version = "3.16.0"
lazy val fs2DomVersion = "0.2.1"
lazy val munitCatsEffectVersion = "2.2.1"

lazy val root =
  tlCrossRootProject.aggregate(`ff4s-heroicons`, examples)

lazy val `ff4s-heroicons` = (project in file("ff4s-heroicons"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    generateHeroicons := {
      import cats.effect.unsafe.implicits.global
      Heroicons
        .generate(
          svgRootPath = "./node_modules/heroicons/",
          baseOutputDirectoryPath =
            "ff4s-heroicons/src/main/scala/ff4s/heroicons",
          baseOutputPackagePath = "ff4s.heroicons"
        )
        .unsafeRunSync()
    }
  )
  .settings(
    name := "ff4s-heroicons",
    libraryDependencies ++= Seq(
      "io.github.buntec" %%% "ff4s" % ff4sVersion,
      "org.scala-js" %%% "scalajs-dom" % scalajsDomVersion,
      "org.typelevel" %%% "munit-cats-effect" % munitCatsEffectVersion % Test
    ),
    Test / jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(
      org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
        .Config()
        .withArgs(List("--enable-source-maps"))
    )
  )

lazy val examples = (project in file("examples"))
  .enablePlugins(ScalaJSPlugin, NoPublishPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    // link to a Scala-version-independent path, so index.html need not change
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory :=
      target.value / "scalajs",
    libraryDependencies ++= Seq(
      "io.github.buntec" %%% "ff4s" % ff4sVersion,
      "org.scala-js" %%% "scalajs-dom" % scalajsDomVersion,
      "com.armanbilge" %%% "fs2-dom" % fs2DomVersion,
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "org.typelevel" %%% "cats-effect-kernel" % catsEffectVersion,
      "org.typelevel" %%% "cats-effect-std" % catsEffectVersion,
      "co.fs2" %%% "fs2-core" % fs2Version
    )
  )
  .dependsOn(`ff4s-heroicons`)
