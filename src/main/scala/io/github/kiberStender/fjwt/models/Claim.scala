package io.github.kiberStender
package fjwt
package models

/** Represents the standard registered claims of a JSON Web Token (JWT) as
  * defined by RFC 7519.
  *
  * Claims are statements about an entity (typically, the user) and additional
  * metadata. All standard claims are optional.
  *
  * @tparam T
  *   The data type used to represent time-based claims (e.g., `Long` for Unix
  *   timestamps, or a time object like `java.time.Instant`). It is covariant
  *   (`+T`) to allow for flexible subtyping.
  */
trait Claim[+T] {

  /** Issuer (`iss`): Identifies the principal (server or application) that
    * issued the JWT.
    */
  def iss: Option[String]

  /** Subject (`sub`): Identifies the principal that is the subject of the JWT
    * (often a user ID).
    */
  def sub: Option[String]

  /** Audience (`aud`): Identifies the intended recipients or target
    * applications of the JWT.
    */
  def aud: Option[String]

  /** Expiration Time (`exp`): The exact time on or after which the JWT must not
    * be accepted for processing.
    */
  def exp: Option[T]

  /** Not Before (`nbf`): The exact time before which the JWT must not be
    * accepted for processing.
    */
  def nbf: Option[T]

  /** Issued At (`iat`): The time at which the JWT was created and issued.
    */
  def iat: Option[T]

  /** JWT ID (`jti`): A unique identifier for the token, typically used to
    * prevent replay attacks.
    */
  def jti: Option[String]
}
