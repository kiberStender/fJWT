package br.com.fjwt

import io.circe.*, io.circe.generic.semiauto.*
import br.com.fjwt.crypto.hs.HmacEncoderAlgorithms

sealed trait JWTHeader
case object EmptyHeader extends JWTHeader
case class Alg(alg: HmacEncoderAlgorithms, typ: String) extends JWTHeader

object JWTHeader {
  given Codec[HmacEncoderAlgorithms] = io.circe.generic.semiauto.deriveCodec

  given encoder: Encoder[Alg] =
    Encoder.forProduct2("alg", "typ")(h => (h.alg.alg, h.typ))
  given decoder: Decoder[Alg] =
    Decoder.forProduct2("alg", "typ")(Alg.apply)
}
