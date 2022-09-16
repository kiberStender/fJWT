import sbt._

object Dependencies {
  case object `commons-codec` {
    val `commons-codec` = "commons-codec" % "commons-codec" % "1.15"
  }
  case object ch {
    case object qos {
      case object logback {
        val `logback-classic` = "ch.qos.logback" % "logback-classic" % "1.2.11"
      }
    }
  }
  case object com {
    case object github {
      case object `julien-truffaut` {
        val `monocle-core`  = "com.github.julien-truffaut" %% "monocle-core"  % "3.0.0-M6"
        val `monocle-macro` = "com.github.julien-truffaut" %% "monocle-macro" % "3.0.0-M6"
        val `monocle-law` = "com.github.julien-truffaut" %% "monocle-law" % "3.0.0-M6" % Test
      }
      case object pureconfig {
        val `pureconfig-core` = "com.github.pureconfig" %% "pureconfig-core" % "0.17.1"
      }
    }
  }
  case object io {
    case object circe {
      val `circe-generic` = "io.circe" %% "circe-generic" % "0.14.2"
      val `circe-parser` = "io.circe" %% "circe-parser" % "0.14.2"
    }

  }
  case object org {
    case object http4s {
      val `http4s-blaze-server` = "org.http4s" %% "http4s-blaze-server" % "0.23.12"
      val `http4s-circe`        = "org.http4s" %% "http4s-circe"        % "0.23.15"
      val `http4s-dsl`          = "org.http4s" %% "http4s-dsl"          % "0.23.15"
    }
    case object scalameta {
      val munit = "org.scalameta" %% "munit" % "0.7.29" % Test
    }
    case object scalatest {
      val scalatest = "org.scalatest" %% "scalatest" % "3.2.13" % Test
    }
    case object scalatestplus {
      val `mockito-4-5` = "org.scalatestplus" %% "mockito-4-5" % "3.2.12.0" % Test
    }
    case object typelevel {
      val `cats-core`   = "org.typelevel" %% "cats-core" % "2.8.0"
      val `cats-effect` = "org.typelevel" %% "cats-effect" % "3.3.14"
    }
  }
}
