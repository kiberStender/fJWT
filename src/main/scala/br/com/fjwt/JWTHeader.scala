package br.com.fjwt

import io.circe.*, io.circe.generic.semiauto.*

sealed trait JWTHeader
case object EmptyHeader extends JWTHeader
case class Alg(alg: JWTAlgorythm, typ: String) extends JWTHeader

object JWTHeader {
  given encoder: Encoder[Alg] =
    Encoder.forProduct2("alg", "typ")(h => (h.alg.alg, h.typ))
  given decoder: Decoder[Alg] =
    Decoder.forProduct2("alg", "typ")(Alg.apply)
}
