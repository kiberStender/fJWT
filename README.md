# F[JWT]

Simple scala 3 library to generate and validate JWT`s (JSON Web Tokens) written in Tagless Final style

For a given hypothetical class Payload that's how one encode and decode it

# Encoding

When encoding an object, even though the library takes care of parsing it to json, you might forget to provide the private key as it usually comes from some config file, therefore the Encoding type is MonadError[F]. This type guarantees you won't lose any errors that might come, and you will be able to treat them(See example below)

In order to instantiate a JWTEncoder to start generating your JWT(JSON Web Token) tokens you will need the following:

- **Base64Encoder[F]** You can either use the already provided Base64Encoder(which is a simple wrapper around ```java.util.Base64``` class) using dsl method to instantiate my implementation where F is just a cats.Applicative or you can easily create your own implementation as Base64Encoder is a trait and only has one method called `encode(str: String): F[String]`

- **HmacEncoder[F]** HMAC implementation (which is n wrapper to org.apache.commons.codec.digest.HmacUtils) where F is a cats.Applicative or you can easily create your own by implementing the trait HmacEncoder which has two methods: `def encode(privateKey: String)(str: String): F[String]` and `def alg: HmacEncoderAlgorithms` which is a method that tells which algorithm will actually be used to populate JWTHeader

After providing these two dependencies you can easily create an instance of JWTEncoder by calling the `def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](base64Encoder: Base64Encoder[F], hsEncoder: HmacEncoder[F]): JWTEncoder[F]` method and by providing any instance of MonadError[F] as cited above. After you have your instance of JWTEncoder, you can create your JWT Token by using one of the two encode methods:

- `def encode[P: Codec](privateKey: String)(iss: Option[String],sub: Option[String],aud: Option[String],exp: Option[LocalDateTime],nbf: Option[LocalDateTime],iat: Option[LocalDateTime],jti: Option[String])(payload: P)(using ZoneId): F[String]`:

  - **P** It is the type of the class you want to encode in the payload of your JWT. It needs a ```io.Circe.Codec``` instance for the JWTEncoder to be able to convert it to a JSON String

  - **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

  - **iss: Option[String]** Optional issuer

  - **sub: Option[String]** Optional subject

  - **aud: Option[String]** Optional intended audience

  - **exp: Option[LocalDateTime]** Optional expiration time

  - **nbf: Option[LocalDateTime]** Optional not before time

  - **iat: Option[LocalDateTime]** Optional issued at time

  - **jti: Option[String]** Optional JWT ID

  - **payload: P** It is the payload itself. An instance of P that you have to provide

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting the LocalDateTime objects to Epoch milli

- `def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String]`:

  - **P** It is the type of the class you want to encode in the payload of your JWT. It needs a ```io.Circe.Codec``` instance for the JWTEncoder to be able to convert it to a JSON String

  - **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

  - **payload: P** It is the payload itself. An instance of P that you have to provide

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting iat to Epoch milli that you can control

Example:

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId
import java.time.Instant

import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.JWTEncoder

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
lazy val hs512Encoder: HmacEncoder[F] = HmacEncoder.hs512Encoder
lazy val encoder: JWTEncoder[F] = JWTEncoder.dsl(base64Encoder, hs512Encoder)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Encoder[Payload] = io.circe.generic.semiauto.deriveCodec

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = Some("1234567890")
val iatEpochMilli: Long = 1516239022
val expEpochMilli: Long = 1516239150
val iat: Option[LocalDateTime] = Some(Instant.ofEpochMilli(iatEpochMilli).atZone(zoneId).toLocalDateTime())
val exp: Option[LocalDateTime] = Some(Instant.ofEpochMilli(expEpochMilli).atZone(zoneId).toLocalDateTime())

val encoded = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

```

# Decoding

When decoding a given JWT(JSON Web Token) you can fall into 4 possible errors:

- **Token does not contain exact 3 parts** `The token must have 3 parts header.payload.signature. If there is more or less than 3 it is considered invalid`
- **Invalid Signature** `It might be a fraud or a mistake, so the signature might not match`
- **Expired Token** `The token might have an exp field indicating when it will expire and by the time you are decoding it, it might be already expired`
- **Payload Decoding failure** `During the parsing of the payload some error may occur like bad payload format`

In order to instantiate a JWTDecoder you will need:

- **PayloadExtractor[F]** You can easily create your own implementation as PayloadExtractor is a trait and only has one method called `def extract[P: Codec](privateKey: String)(token: String)(using ZoneId): F[P]` 
or use one of the already provided PayloadExtractor implementations using either 
  - `def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](base64Decoder: Base64Decoder[F]): PayloadExtractor[F]`: Uses the header to figure out which Hmac algorithm was used
  - `def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]](base64Decoder: Base64Decoder[F], hmacEncoder: HmacEncoder[F]): PayloadExtractor[F]`: Completely ignores the header and uses the hmacEncoder implementation you provided yourself

The Second instantiation method is recommended if your JWT(Json Web Token) alg field inside the header(`{"alg": "Alg_name", "typ": "JWT"}`) does not have a value matching one of `org.apache.commons.codec.digest.HmacAlgorithms` values

Once instantiated this is the only method JWTDecoder has:

- `def decode[P: Codec](privateKey: String)(token: String)(using ZoneId): F[P]`:

  - **P** It is the type of the class you want to decode after the JWT is completely decoded and is valid. It needs a io.Circe.Codec instance for the JWTDecoder be able to convert it from a JSON String

  - **privateKey: String** This is the key that will be used to check wether the provided signature matches with the actual encription of the header and payload

  - **token: String** The token that will be decoded and in case it succeeds its payload will be returned

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting iat from Epoch milli to java.time.LocalDateTime in a way that you can control the time zone

Example: Using the header to figure or the hmac algorithm

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId

import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.utils.PayloadExtractor
import io.github.kiberStender.fjwt.JWTDecoder

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val payloadExtractor: PayloadExtractor[F] = PayloadExtractor.dsl(base64Decoder)
lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(payloadExtractor)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

val key = "kleber-super-secret-key"
val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.29743acd123183547fe568550db9416a02ed3a7aeeb9c4a74ec73baf02bb688b58d58f6508f7f0ab956a22aaf619602359038abae336abf0e0ceb7b0a0268b64"

val decoded: F[Payload] = decoder.decode(key)(input)

```

Example: Ignoring the header and providing the Hmac instance yourslef

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId

import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.utils.PayloadExtractor
import io.github.kiberStender.fjwt.JWTDecoder

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val hmacEncoder: HmacEncoder[F] = HmacEncoder.dsl
lazy val payloadExtractor: PayloadExtractor[F] = PayloadExtractor.dsl(base64Decoder, hmacEncoder)
lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(payloadExtractor)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

val key = "kleber-super-secret-key"
val input = "eyJhbGciOiJIbWFjU0hBNTEyIiwidHlwIjoiSldUIn0=.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwicGF5bG9hZCI6eyJuYW1lIjoiSm9obiBEb2UiLCJhZG1pbiI6dHJ1ZX19.29743acd123183547fe568550db9416a02ed3a7aeeb9c4a74ec73baf02bb688b58d58f6508f7f0ab956a22aaf619602359038abae336abf0e0ceb7b0a0268b64"

val decoded: F[Payload] = decoder.decode(key)(input)

```

# Using the library

In order to use this library just add it to your build dependency list 
```scala
libraryDependencies += "io.github.kiberStender" %% "fjwt" % "1.0.1"
```