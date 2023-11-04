package io.github.kiberStender
package fjwt
package model

import io.circe.Codec
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
  */
final case class Claim(
    iss: Option[String],
    sub: Option[String],
    aud: Option[String],
    exp: Option[Long],
    nbf: Option[Long],
    iat: Option[Long],
    jti: Option[String]
)

/** Implicit converters for circe
  */
object Claim:
  given Codec[Claim] = io.circe.generic.semiauto.deriveCodec
