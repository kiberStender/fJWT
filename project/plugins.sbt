ThisBuild / useSuperShell := false
ThisBuild / autoStartServer := false

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.0.3")