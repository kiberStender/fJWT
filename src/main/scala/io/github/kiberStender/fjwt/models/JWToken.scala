package io.github.kiberStender
package fjwt
package models

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

/** A representation of the JSON Web Token(JWT) after being parsed and validated
  *
  * @param header
  *   The first part of the token, containing the algorithm used to sign the
  *   token
  * @param claim
  *   The metadata of the token containing when the token was issued and when it
  *   is going to expire
  * @param payload
  *   The data the user wanted to transmit
  * @tparam C
  *   The type of the claim
  * @tparam P
  *   The type of the payload
  */
case class JWToken[T, P](
    header: HmacAlgorithm,
    claim: Claim[T],
    payload: P
)
