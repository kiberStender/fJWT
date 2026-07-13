package io.github.kiberStender
package fjwt
package header

import cats.MonadError
import cats.syntax.all.{catsSyntaxApplicativeId, catsSyntaxApplicativeErrorId, toFlatMapOps}
import io.github.kiberStender.fjwt.exception.JWTError.InvalidAlgError
import io.github.kiberStender.fjwt.extractField
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.*

object AlgTasks:
  extension (algJsonStr: String) {

    /** A method to extract the Hmac algorithm embed in first part of the JWT
      *
      * @tparam F
      *   The effect type
      * @return
      *   Either an instance of [[HmacAlgorithm]] or an [[InvalidAlgError]]
      */
    private[fjwt] def extractAlg[F[*]: [F[*]] =>> MonadError[F, Throwable]]: F[HmacAlgorithm] =
      extractField[F](""""alg"\s*:\s*"([^"]+)"""".r)(algJsonStr).flatMap {
        case Some("HS1")   => (HmacSHA1: HmacAlgorithm).pure[F]
        case Some("HS224") => (HmacSHA224: HmacAlgorithm).pure[F]
        case Some("HS256") => (HmacSHA256: HmacAlgorithm).pure[F]
        case Some("HS384") => (HmacSHA384: HmacAlgorithm).pure[F]
        case Some("HS512") => (HmacSHA512: HmacAlgorithm).pure[F]
        case Some(algStr)  => InvalidAlgError(algStr).raiseError[F, HmacAlgorithm]
        case None          => InvalidAlgError("Empty value").raiseError[F, HmacAlgorithm]
      }
  }
