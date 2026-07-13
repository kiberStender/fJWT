package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

/** Trait to implement a hashing algorithm
  * @tparam F
  *   The effect type
  */
trait Hmac[F[*]] {

  /** Method to hash a given string using a given hmac algorithm
    * @param hmac
    *   The algorithm to be used
    * @param privateKey
    *   The key used to hash the string
    * @param str
    *   The string to be hashed
    * @return
    *   either a byte array or whatever error that might be thrown
    */
  def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]]
}
