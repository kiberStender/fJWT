package br.com.fjwt

import io.circe.*, io.circe.generic.semiauto.*

enum JWTAlgorythm(val alg: String):
  case HS256 extends JWTAlgorythm("HS256")
  case HS512 extends JWTAlgorythm("HS512")

object JWTAlgorythm {
  given encoder: Encoder[JWTAlgorythm] = deriveEncoder[JWTAlgorythm]
  given decoder: Decoder[JWTAlgorythm] = deriveDecoder[JWTAlgorythm]
}
