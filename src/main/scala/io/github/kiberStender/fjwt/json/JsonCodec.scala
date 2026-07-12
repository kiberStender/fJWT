package io.github.kiberStender
package fjwt
package json

trait JsonDecoder[F[*], J] {
  def decode(json: String): F[J]
}

trait JsonEncoder[F[*], J] {
  def encode(json: J): F[String]
}

trait JsonCodec[F[*], J] extends JsonEncoder[F, J] with JsonDecoder[F, J]
