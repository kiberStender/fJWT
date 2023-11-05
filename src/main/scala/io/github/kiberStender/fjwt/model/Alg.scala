package io.github.kiberStender
package fjwt
package model

import io.circe.generic.semiauto.deriveCodec
import io.circe.Codec
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.*

/** A case class to map the algorithm part of the JWT Header
  * @param alg
  *   The Almorithm itself
  * @param typ
  *   the type of the Token, which is always JWT
  */
case class Alg(alg: String, typ: String)

/** Implicit conversions for circe
  */
object Alg:

  given codec: Codec[Alg] = deriveCodec

  extension (hmacAlgorithm: HmacEncoderAlgorithms) {
    def toAlg: Alg = Alg(hmacAlgorithm.alg, "JWT")
  }
  extension (alg: Alg) {
    def toHmacAlgorithms: HmacEncoderAlgorithms = alg match {
      case Alg("HS1", _) => HmacSHA1
      case Alg("HS224", _) => HmacSHA224
      case Alg("HS256", _) => HmacSHA256
      case Alg("HS384", _) => HmacSHA384
      case Alg("HS512", _) => HmacSHA512
    }

    def toJson: String = codec(alg).noSpaces
  }
