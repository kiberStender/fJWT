import sbt._

object Dependencies {
  case object `commons-codec` {
    val `commons-codec` = "commons-codec" % "commons-codec" % "1.16.0"
  }
  case object io {
    case object circe {
      val `circe-generic` = "io.circe" %% "circe-generic" % "0.14.5"
      val `circe-parser` = "io.circe" %% "circe-parser" % "0.14.5"
    }

  }
  case object org {
    case object scalatest {
      val scalatest = "org.scalatest" %% "scalatest" % "3.2.15" % Test
    }
    case object typelevel {
      val `cats-core`   = "org.typelevel" %% "cats-core" % "2.9.0"
    }
  }
}
