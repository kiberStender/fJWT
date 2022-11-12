import xerial.sbt.Sonatype._

ThisBuild / organization := "io.github.kiberStender"
ThisBuild / description := "Simple Scala 3 JWT encoder/decoder written using Tagless final encoding"
homepage := Some(url("https://github.com/kiberStender/fJWT"))
scmInfo := Some(ScmInfo(url("https://github.com/kiberStender/fJWT"), "git@github.com:kiberStender/fJWT.git"))
developers := List(Developer("kiberStender", "Kleber Stender", "kleberstenderdev@gmail.com", url("https://github.com/kiberStender/")))
licenses += ("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

publishMavenStyle := true
sonatypeProfileName := "io.github.kiberStender"
sonatypeProjectHosting := Some(GitHubHosting(user = "kiberStender", repository = "fjwt", email = "kleberstenderdev@gmail.com"))

publishTo := sonatypePublishToBundle.value

sonatypeCredentialHost := "s01.oss.sonatype.org"

versionScheme := Some("early-semver")

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