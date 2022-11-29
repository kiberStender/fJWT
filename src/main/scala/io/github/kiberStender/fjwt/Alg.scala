package io.github.kiberStender
package fjwt

import io.circe.{Encoder, Decoder}
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.*

/** A case class to map the algorithm part of the JWT Header
  * @param alg
  *   The Almorithm itself
  * @param typ
  *   the type of the Token, which is always JWT
  */
private case class Alg(alg: HmacEncoderAlgorithms, typ: String)

/** Implicit conversions for circe
  */
private object Alg:
  given encoder: Encoder[Alg] =
    Encoder.forProduct2("alg", "typ")(h => (h.alg.alg, h.typ))
  given decoder: Decoder[Alg] =
    Decoder.forProduct2("alg", "typ")(Alg.apply)
