package io.github.kiberStender
package fjwt
package json

/** A typeclass defining an effectful operation for deserializing a JSON string
  * into a strongly-typed domain model.
  *
  * Within the FJWT library, this trait abstracts the JSON parsing layer,
  * allowing you to plug in your preferred JSON library (such as Circe,
  * Play-JSON, or ZIO-JSON). It is used primarily during JWT decoding to
  * deserialize the raw JSON strings extracted from the Header and Payload
  * segments.
  *
  * By returning the deserialized object wrapped in the effect type `F`,
  * implementations can gracefully handle parsing or mapping failures (such as
  * missing fields, type mismatches, or malformed JSON syntax) by lifting
  * exceptions into the effect system (e.g., via `MonadError` or
  * `ApplicativeError`) rather than throwing runtime errors.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  * @tparam J
  *   The target domain type being decoded from JSON.
  *
  * @example
  *   {{{
  * // Scala 3 example implementing JsonDecoder using Circe and ApplicativeError
  * import io.circe.Decoder
  * import io.circe.parser
  * import cats.ApplicativeError
  * import cats.syntax.all.*
  *
  * given [F[_], J](using decoder: Decoder[J], ae: ApplicativeError[F, Throwable]): JsonDecoder[F, J] =
  *   (json: String) => parser.decode[J](json) match
  *     case Left(err)    => err.raiseError[F, J]
  *     case Right(value) => value.pure[F]
  *   }}}
  */
trait JsonDecoder[F[*], J] {

  /** Deserializes a raw JSON string into a value of type `J`.
    *
    * @param json
    *   The raw JSON string to be decoded.
    * @return
    *   The deserialized instance of type `J`, suspended in the effect `F`.
    */
  def decode(json: String): F[J]
}

/** A typeclass defining an effectful operation for serializing a strongly-typed
  * domain model into a JSON string.
  *
  * Within the FJWT library, this trait abstracts the JSON serialization layer,
  * allowing you to integrate any JSON library of choice (such as Circe,
  * Play-JSON, or ZIO-JSON). It is used primarily during JWT encoding to
  * serialize your Header, Claim, and Payload domain models into the JSON
  * strings that form the components of a JWT.
  *
  * By wrapping the resulting JSON string in the effect type `F`, serialization
  * can be safely composed alongside Base64 encoding and cryptographic signing
  * within your monadic workflows.
  *
  * @tparam F
  *   The effect type constructor (e.g., `cats.effect.IO`, `scala.util.Try`).
  * @tparam J
  *   The domain type being serialized to JSON.
  *
  * @example
  *   {{{
  * // Scala 3 example implementing JsonEncoder using Circe and Applicative
  * import io.circe.Encoder
  * import io.circe.syntax.*
  * import cats.Applicative
  * import cats.syntax.all.*
  *
  * given [F[_], J](using encoder: Encoder[J], a: Applicative[F]): JsonEncoder[F, J] =
  *   (json: J) => json.asJson.noSpaces.pure[F]
  *   }}}
  */
trait JsonEncoder[F[*], J] {

  /** Serializes an instance of type `J` into a raw JSON string.
    *
    * @param json
    *   The domain object to be serialized.
    * @return
    *   The resulting JSON string, suspended in the effect `F`.
    */
  def encode(json: J): F[String]
}
