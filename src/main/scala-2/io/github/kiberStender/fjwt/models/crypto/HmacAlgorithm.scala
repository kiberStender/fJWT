package io.github.kiberStender
package fjwt
package models
package crypto

/** Trait representing the supported Hmac algorithms
  */
sealed trait HmacAlgorithm {
  def alg: String
  def fullName: String
}

object HmacAlgorithm {

  case object HmacSHA1 extends HmacAlgorithm {
    val alg = "HS1"
    val fullName = "HmacSHA1"
  }

  case object HmacSHA224 extends HmacAlgorithm {
    val alg = "HS224"
    val fullName = "HmacSHA224"
  }

  case object HmacSHA256 extends HmacAlgorithm {
    val alg = "HS256"
    val fullName = "HmacSHA256"
  }

  case object HmacSHA384 extends HmacAlgorithm {
    val alg = "HS384"
    val fullName = "HmacSHA384"
  }

  case object HmacSHA512 extends HmacAlgorithm {
    val alg = "HS512"
    val fullName = "HmacSHA512"
  }
}
