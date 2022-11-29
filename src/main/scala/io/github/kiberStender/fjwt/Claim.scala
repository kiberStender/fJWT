package io.github.kiberStender
package fjwt

import io.circe.{Codec, Decoder, Encoder, Json, HCursor}
import io.circe.Decoder.Result
import io.circe.syntax.EncoderOps

/** A case class to map the claim part of the JWT
  * @param iss
  *   The issuer of the token[Optional]
  * @param sub
  *   The subject of the token[Optional]
  * @param aud
  *   The intended audience of the token[Optional]
  * @param exp
  *   The expiration time of the token[Optional]
  * @param nbf
  *   The not before datetime(The token cannot be used before the given date) of the token[Optional]
  * @param iat
  *   The Issued at(The time the token was issued) of the token[Optional]
  * @param jti
  *   The token ID[Optional]
  * @param payload
  *   The data you want to transmit itself
  * @param codec$P$0
  *   The implicit Codec instance to help encode the payload
  * @tparam P
  *   The type of the Payload
  */
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

/** Implicit converters for circe
  */
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
