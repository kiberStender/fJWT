package io.github.kiberStender
package fjwt
package claim

import io.github.kiberStender.fjwt.models.Claim

trait ToLong[F[*], T] {
  def toLong(claim: Claim[T]): F[Claim[Long]]
}
