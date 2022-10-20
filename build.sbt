ThisBuild / organization := "br.com.fjwt"
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val Cctt = "compile->compile;test->test"

lazy val domain = (project in file("01-domain"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Common.`domain-dependencies`)

lazy val core =
  project
    .in(file("02-core"))
    .dependsOn(domain % Cctt)
    .settings(Common.settings: _*)
    .settings(libraryDependencies ++= Common.`core-dependencies`)

  lazy val root = (project in file("."))
    .dependsOn(core % Cctt)
    .settings(name := "fJWT")
    .aggregate(
      domain,
      core
    )
    .settings(Common.settings: _*)
    .settings(libraryDependencies ++= Common.`jwt-dependencies`)
    .enablePlugins(JavaAppPackaging)