import sbt.{Def, _}
import Keys._
import Dependencies.{io, _}

object Common {
  val scala2Version = "2.13.10"
  val scala3Version = "3.1.3"

  lazy val settings: Seq[Def.Setting[_ >: String with Seq[String] with Task[Seq[File]] with Boolean]] = Seq(
    // To make the default compiler and REPL use Dotty
    scalaVersion := scala3Version,

    // To cross compile with Scala 3 and Scala 2
    crossScalaVersions := Seq(scala2Version, scala3Version),

    Global / transitiveClassifiers           := Seq(Artifact.SourceClassifier),
    Compile / doc / sources                  := Nil,
    Compile / packageDoc / publishArtifact   := false,
    Test / parallelExecution                 := true
  )

  lazy val dependencies: Seq[ModuleID] = Seq(
    org.typelevel.`cats-core`,
    `commons-codec`.`commons-codec`,
    io.circe.`circe-parser`,
    io.circe.`circe-generic`,
    // Test
    org.scalatest.scalatest
  )
}
