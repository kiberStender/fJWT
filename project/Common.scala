import sbt.{Def, *}
import Keys.*
import Dependencies.{io, *}

import scala.collection.immutable

object Common {
  lazy val scala3Version = "3.3.8"
  lazy val scala2Version = "2.13.18"

  lazy val supportedScalaVersions: immutable.Seq[String] = List(scala2Version, scala3Version)

  lazy val settings: Seq[Def.Setting[? >: String & Seq[String] & Task[Seq[String]] & Boolean]] = Seq(
    scalaVersion                    := scala3Version,
    crossScalaVersions              := supportedScalaVersions,
    Global / scalacOptions          := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq("-source:future")
      case Some((2, _)) => Seq("-P:kind-projector:underscore-placeholders", "-Ymacro-annotations")
      case _                       => Nil
    }),
    Global / transitiveClassifiers  := Seq(Artifact.SourceClassifier),
    Test / parallelExecution        := true
  )

  lazy val dependencies: Seq[ModuleID] = Seq(
    org.typelevel.`cats-core`,
    org.typelevel.`cats-effect`,
    `commons-codec`.`commons-codec`,
    // Test
    org.scalatest.scalatest,
    io.circe.`circe-parser`  % Test,
    io.circe.`circe-generic`  % Test,
  )
}
