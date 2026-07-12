import xerial.sbt.Sonatype._
import Dependencies.{io, *}

ThisBuild / organization := "io.github.kiberStender"
ThisBuild / description := "Simple Scala 3 JWT encoder/decoder written using Tagless final encoding"
ThisBuild / homepage := Some(url("https://github.com/kiberStender/fJWT"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/kiberStender/fJWT"), "git@github.com:kiberStender/fJWT.git"))
ThisBuild / developers := List(Developer(id = "1076952", name = "Kleber Eduardo Scalise Stender", email = "kleberstenderdev@gmail.com", url = url("https://github.com/kiberStender")))
ThisBuild / licenses += ("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / publishMavenStyle := true
ThisBuild / sonatypeProfileName := "io.github.kiberStender"
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting(user = "kiberStender", repository = "fjwt", email = "kleberstenderdev@gmail.com"))

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

ThisBuild / versionScheme := Some("early-semver")

ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

// release with sbt-release plugin
import ReleaseTransformations._
ThisBuild / releaseCrossBuild := true
//releaseTagName := s"version-${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
ThisBuild / releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val root = (project in file("."))
  .settings(name := "fJWT")
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Common.dependencies)
  .settings(
    assembly / assemblyJarName := s"fjwt_${scalaBinaryVersion.value}-${version.value}.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _ @ _*) => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
  .settings(libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        List(
          compilerPlugin(org.typelevel.`kind-projector`),
          compilerPlugin(com.olegpy.`better-monadic-for`)
        )
      case _                       => Nil
    }
  })