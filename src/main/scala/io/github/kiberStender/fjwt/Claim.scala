package io.github.kiberStender
package fjwt

import io.circe.*
import io.circe.Decoder.Result
import io.circe.syntax.*

private final case class Claim[P: Codec](
    iss: Option[String],
    sub: Option[String],
    aud: Option[String],
    exp: Option[Long],
    nbf: Option[Long],
    iat: Option[Long],
    jti: Option[String],
    payload: P
)

private object Claim:
  given encoder[P: Codec](using EP: Encoder[P]): Encoder[Claim[P]] =
    new Encoder[Claim[P]]() {
      def apply(cp: Claim[P]): Json = Json.obj(
        "iss" -> cp.iss.asJson,
        "sub" -> cp.sub.asJson,
        "aud" -> cp.aud.asJson,
        "exp" -> cp.exp.asJson,
        "nbf" -> cp.nbf.asJson,
        "iat" -> cp.iat.asJson,
        "jti" -> cp.jti.asJson,
        "payload" -> EP(cp.payload)
      )
    }
  given decoder[P: Codec]: Decoder[Claim[P]] = new Decoder[Claim[P]]() {
    def apply(c: HCursor): Result[Claim[P]] =
      for
        iss <- c.downField("iss").as[Option[String]]
        sub <- c.downField("sub").as[Option[String]]
        aud <- c.downField("aud").as[Option[String]]
        exp <- c.downField("exp").as[Option[Long]]
        nbf <- c.downField("nbf").as[Option[Long]]
        iat <- c.downField("iat").as[Option[Long]]
        jti <- c.downField("jti").as[Option[String]]
        payload <- c.downField("payload").as[P]
      yield Claim(iss, sub, aud, exp, nbf, iat, jti, payload)
  }
