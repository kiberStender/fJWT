package io.github.kiberStender
package fjwt
package claim

import io.github.kiberStender.fjwt.models.Claim

trait FromLong[F[*], T] {
  def fromLong(claim: Claim[Long]): F[Claim[T]]
}
