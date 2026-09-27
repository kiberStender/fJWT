package io.github.kiberStender
package fjwt
package models

/** A full representation of the JSON Web Token (JWT) after being parsed and
  * validated.
  *
  * @param header
  *   The first part of the token, containing the algorithm used to sign the
  *   token
  * @param claim
  *   The metadata of the token containing when the token was issued and when it
  *   is going to expire
  * @param payload
  *   The data the user wants to transmit
  * @tparam H
  *   The specific type of the header, which must be a subtype of `Header`.
  * @tparam C
  *   The specific type of the claims set, which must be a subtype of
  *   `Claim[Any]`.
  * @tparam P
  *   The data type of the custom payload (e.g., a JSON object, a String, or a
  *   specific case class).
  */
case class JWToken[H <: Header, C <: Claim[Any], P](
    header: H,
    claim: C,
    payload: P
)
