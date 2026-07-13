package io.github.kiberStender
package fjwt
package models

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
  *   The not before datetime(The token cannot be used before the given date) of
  *   the token[Optional]
  * @param iat
  *   The Issued at(The time the token was issued) of the token[Optional]
  * @param jti
  *   The token ID[Optional]
  */
final case class Claim[+T](
    iss: Option[String] = None,
    sub: Option[String] = None,
    aud: Option[String] = None,
    exp: Option[T] = None,
    nbf: Option[T] = None,
    iat: Option[T] = None,
    jti: Option[String] = None
)
