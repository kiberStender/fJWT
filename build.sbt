ThisBuild / organization := "io.github.kiberStender"
ThisBuild / description := "Simple Scala 3 JWT encoder/decoder written using Tagless final encoding"
ThisBuild / homepage := Some(url("https://github.com/kiberStender/fJWT"))
ThisBuild / scmInfo := Some(ScmInfo(url("https://github.com/kiberStender/fJWT"), "git@github.com:kiberStender/fJWT.git"))
ThisBuild / developers := List(Developer("kiberStender", "Kleber Stender", "kleber.stender@gmail.com", url("https://github.com/kiberStender/")))
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeOssSnapshots.head else Opts.resolver.sonatypeOssReleases.head)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / versionScheme := Some("early-semver")

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

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
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeRelease"),
  pushChanges
)

lazy val root = (project in file("."))
  .settings(name := "fJWT")
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Common.dependencies)
  .enablePlugins(JavaAppPackaging)