ThisBuild / organization := "br.com.fjwt"
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / version := "0.0.1-SNAPSHOT"

lazy val Cctt = "compile->compile;test->test"

  lazy val crypto= (project in file("00-crypto"))
    .settings(Common.settings: _*)
    .settings(libraryDependencies ++= Common.`crypto-dependencies`)

  lazy val root = (project in file("."))
    .dependsOn(crypto % Cctt)
    .settings(name := "fJWT")
    .settings(Common.settings: _*)
    .settings(libraryDependencies ++= Common.`jwt-dependencies`)
    .enablePlugins(JavaAppPackaging)    