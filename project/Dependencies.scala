import sbt._

object Dependencies {

  case object com {
    case object olegpy {
      val `better-monadic-for` = "com.olegpy" %% "better-monadic-for" % "0.3.1"
    }
  }

  case object `commons-codec` {
    val `commons-codec` = "commons-codec" % "commons-codec" % "1.22.0"
  }

  case object io {
    case object circe {
      val `circe-generic` = "io.circe" %% "circe-generic" % "0.14.16"
      val `circe-parser` = "io.circe" %% "circe-parser" % "0.14.16"
    }
  }

  case object org {
    case object scalatest {
      val scalatest = "org.scalatest" %% "scalatest" % "3.2.20" % Test
    }

    case object scalatestplus {
      val `mockito-4-5` = "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % Test
    }
    case object typelevel {
      val `cats-core`   = "org.typelevel" %% "cats-core" % "2.13.0"
      val `cats-effect` = "org.typelevel" %% "cats-effect" % "3.7.0"

      val `kind-projector` = "org.typelevel" % "kind-projector" % "0.13.4" cross CrossVersion.full
    }
  }
}
