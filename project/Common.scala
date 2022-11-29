import sbt.{Def, _}
import Keys._
import Dependencies.{io, _}

object Common {
  val scala3Version = "3.2.1"

  lazy val settings = Seq(
    scalaVersion := scala3Version,
    Global / scalacOptions                   := Seq("-source:future"),
    Global / transitiveClassifiers           := Seq(Artifact.SourceClassifier),
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
