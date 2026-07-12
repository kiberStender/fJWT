package io.github.kiberStender
package fjwt
package crypto
package hmac

import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm

trait Hmac[F[*]] {
  def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]]
}
