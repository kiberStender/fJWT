# fJWT

Simple scala 3 library to generate and validate JWT`s (JSON Web Tokens) written in Tagless Final style

For a given hypotetical class Payload that's how one encode and decode it

# Encoding

When encoding an object, even though the library takes care of parsing it to json, in the end it is still a String encriptation, so no error is expected, that's why it expects a simple Monad[F] where F is of the type `F = [T] =>> T`, which is an ID[T] type, meaning any class that implements Monad will sufice. In the above example I'm using cats ID typeclass implicity, but I could literally use an Option, Either, Future, Java Promise, ZIO IO, Monix task, etc....

In order to instantiate a JWTEncoder to start generating your JWT tokens you will need the following:

- **Base64Encoder[F]** You can either use the already provided Base64Encoder(which is a simple wrapper around java.util.Base64 class) using dsl method to instantiate my implementation where F is just a cats.Applicative or you can easily create your own implementation as Base64Encoder is a trait and only has one method called `encode(str: String): F[String]`

- **HmacEncoder[F]** HMAC implementation (which is an wrapper to org.apache.commons.codec.digest.HmacUtils) where F is a cats.Applicative or you can easily create your own by implementing the trait HmacEncoder which has two methods: `def encode(privateKey: String)(str: String): F[String]` and `def alg: HmacEncoderAlgorithms` which is a method that tells which algorithm will actually be used and to populate JWTHeader

After providing these two dependencies you can easily create an instance of JWTEncoder by calling the `dsl[F[*]: Monad](base64Encoder: Base64Encoder[F], hmacEncoder: HmacEncoder[F]): JWTEncoder[F]` method and by providing any instance of Monad[F] as cited above. After you have your instance of JWTEncoder you can create your JWT Token by using one of the two encode methods:

- `def encode[P: Codec](privateKey: String)(iss: Option[String],sub: Option[String],aud: Option[String],exp: Option[LocalDateTime],nbf: Option[LocalDateTime],iat: Option[LocalDateTime],jti: Option[String])(payload: P)(using ZoneId): F[String]`:

  - **P** It is the type of the class you want to encode in the payload of your JWT. It needs a io.Circe.Codec instance for the JWTEncoder to be able to convert it to a JSON String

  - **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

  - **iss: Option[String]** Optional issuer

  - **sub: Option[String]** Optional subject

  - **aud: Option[String]** Optional intended audience

  - **exp: Option[LocalDateTime]** Optional expiration time

  - **nbf: Option[LocalDateTime]** Optional not before time

  - **iat: Option[LocalDateTime]** Optional issued at time

  - **jti: Option[String]** Optional JWT ID

  - **payload: P** It is the payload itself. An instance of P that you have to provide

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting the LocaldateTime objects to Epoch milli

- `def encode[P: Codec](privateKey: String)(payload: P)(using ZoneId): F[String]`:

  - **P** It is the type of the class you want to encode in the payload of your JWT. It needs a io.Circe.Codec instance for the JWTEncoder to be able to convert it to a JSON String

  - **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

  - **payload: P** It is the payload itself. An instance of P that you have to provide

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting iat to Epoch milli that you can control

Example:

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId
import java.time.Instant

import br.com.fjwt.crypto.base64.Base64Encoder
import br.com.fjwt.crypto.hs.HMacEncoder
import br.com.fjwt.encode.JWTEncoder

type F = [T] =>> T
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
- **Expired Token** `The token might have an exp field indicating when it will expire and at the time you are decoding it, it might be already expired`
- **Payload Decoding failure** `During the parsing of the payload some error may occur like bad payload format`

In order to instantiate a JWTDecoder you will need:

- **Base64Decoder[F]** You can either use the already provided Base64Decoder(which is a simple wrapper around java.util.Base64 class) using dsl method to instantiate my implementation where F is just a cats.ApplicativeError or you can easily create your own implementation as Base64Decoder is a trait and only has one method called `def encode(str: String): F[String]`

- **HmacEncoder[F]** HMAC implementation (which is an wrapper to org.apache.commons.codec.digest.HmacUtils) where F is a cats.Applicative or you can easily create your own by implementing the trait HmacEncoder which has two methods: `def encode(privateKey: String)(str: String): F[String]` and `def alg: HmacEncoderAlgorithms` which is a method that tells which algorithm will actually be used and to populate JWTHeader. It is used to recalculate the token signature using it's header and body to check if the newly calculated signature matches against the token signature

With the dependencies satisfied you can easily call the method `def dsl[F[*]: Monad](base64Encoder: Base64Encoder[F], hsEncoder: HmacEncoder[F]): JWTEncoder[F]` to create an instance of JWTDecoder and then you can decode a given token by using the method decode:

- `def decode[P: Codec](privateKey: String)(token: String)(using ZoneId): F[P]`:

  - **P** It is the type of the class you want to decode after the JWT is completely decoded and is valid. It needs a io.Circe.Codec instance for the JWTDecoder be able to convert it from a JSON String

  - **privateKey: String** This is the key that will be used to check wether the provided signature matches with the actual encription of the header and payload

  - **token: String** The token that will be decoded and in case it succeeds its payload will be returned

  - **ZoneId** An implicit instance of java.time.ZoneId object to help converting iat from Epoch milli to java.time.LocalDateTime in a way that you can control the time zone

Example:

```scala
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId

import br.com.fjwt.crypto.base64.decode.Base64Decoder
import br.com.fjwt.crypto.hs512.HS512Encoder
import br.com.fjwt.decode.JWTDecoder

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val hs512Encoder: HmacEncoder[F] = HmacEncoder.hs512Encoder
lazy val decoder: JWTDecoder[F] = JWTDecoder.dsl(base64Encoder, hs512Encoder)
given zoneId: ZoneId = ZoneId.of("UTC")

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0=.d92964cfa2a75550ae735c371a831e4eeb6c40b1734c28b565ab8fbc8a95b038d9e462c0b78a2c1b8fc00117bd0d7eabe92163b738be84e3181aeaede4f7bae6"
val key = "a-super-secret-key"

val decoded: F[Payload] = decoder.decode(key)(input)

```
