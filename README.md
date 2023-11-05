# F[JWT]

Simple scala 3 library to generate and validate JWT`s (JSON Web Tokens) written in Tagless Final style

For a given hypothetical class Payload that's how one encode and decode it

```scala
final case class Payload(name: String, admin: Boolean)
```

# Encoding

```scala
trait JWTEncoder[F[*], Time, P]:
  def encode(privateKey: String)(
      iss: Option[String] = None,
      sub: Option[String] = None,
      aud: Option[String] = None,
      exp: Option[Time] = None,
      nbf: Option[Time] = None,
      iat: Option[Time] = None,
      jti: Option[String] = None
  )(payload: P): F[String]
```

Encoding is the act of converting a given information into a particular form(in this case a 3 parts string). Even though the library(io.circe) takes care of parsing each part to JSON, you might forget to provide the private key as it usually comes from some config file, therefore the Encoding type is MonadError[F]. This type guarantees you won't lose any errors that might come, and you will be able to treat them(See example below)

In order to instantiate a JWTEncoder to start generating your JWT(JSON Web Token) tokens using one of the two already provided implementations(JWTEncoderLong and JWTEncoderLocalDateTime)  you will need the following:
- **Time** It is the type of time measurement you want to use. It is generic to be flexible to either user any library you want(Joda Time, Java LocalDateTime library, etc) or your own implementation like a simple Long or whatever you need at the moment  

- **P** It is the type of the class you want to encode in the payload of your JWT. It needs a ```io.Circe.Codec``` instance for the JWTEncoder to be able to convert it to a JSON String

- **Base64Encoder[F]** You can either use the already provided Base64Encoder(which is a simple wrapper around ```java.util.Base64``` class) using dsl method to instantiate my implementation where F is just a cats.Applicative or you can easily create your own implementation as Base64Encoder is a trait

- **HmacEncoderAlgorithms** HMAC implementation which is n wrapper to org.apache.commons.codec.digest.HmacUtils. Used to sign the token 

- **ZoneId** An implicit instance of java.time.ZoneId object to help converting the LocalDateTime objects to Epoch milli

After providing these dependencies you can easily create an instance of JWTEncoder by calling either the JWTEncoderLong method 
```scala 3
def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F], hsEncoder: HmacEncoder[F])(using zoneId: ZoneId): JWTEncoder[F, Long, P]
``` 
or JWTEncoderLocalDateTime method 
```scala 3
def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F], hsEncoder: HmacEncoder[F])(using zoneId: ZoneId): JWTEncoder[F, LocalDateTime, P]
``` 
and by providing any instance of MonadError[F] as cited above. After you have your instance of JWTEncoder, you can create your JWT Token by using encode method:

```scala 3 
def encode(privateKey: String)(iss: Option[String], sub: Option[String],  aud: Option[String], exp: Option[Time], nbf: Option[Time], iat: Option[Time], jti: Option[String])(payload: P): F[String]
```

  - **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

  - **iss: Option[String]** Optional issuer

  - **sub: Option[String]** Optional subject

  - **aud: Option[String]** Optional intended audience

  - **exp: Option[Time]** Optional expiration time

  - **nbf: Option[Time]** Optional not before time

  - **iat: Option[Time]** Optional issued at time

  - **jti: Option[String]** Optional JWT ID

  - **payload: P** It is the payload itself. An instance of P that you have to provide

Example with simple Long value:

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA512

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

given Encoder[Payload] = io.circe.generic.semiauto.deriveCodec

given zoneId: ZoneId = ZoneId.of("UTC")

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
private lazy val hmacEncoder: HmacEncoderAlgorithms = HmacSHA512

lazy val encoder: JWTEncoder[F, Long, Payload] = JWTEncoderLong.dsl(base64Encoder, hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = Some("1234567890")
val iat: Option[Long] = Some(1516239022)
val exp: Option[Long] = Some(1516239150)

val encoded = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

```

Example with LocalDateTime value:

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.crypto.base64.Base64Encoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoderAlgorithms.HmacSHA512

import java.time.ZoneId
import java.time.Instant

final case class Payload(name: String, admin: Boolean)

given Encoder[Payload] = io.circe.generic.semiauto.deriveCodec

given zoneId: ZoneId = ZoneId.of("UTC")

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
lazy val hmacEncoder: HmacEncoderAlgorithms = HmacSHA512

lazy val encoder: JWTEncoder[F, LocalDateTime, Payload] = JWTEncoderLocalDateTime.dsl(base64Encoder, hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = Some("1234567890")
val iatEpochMilli: Long = 1516239022
val expEpochMilli: Long = 1516239150
val iat: Option[LocalDateTime] = Some(Instant.ofEpochMilli(iatEpochMilli).atZone(zoneId).toLocalDateTime())
val exp: Option[LocalDateTime] = Some(Instant.ofEpochMilli(expEpochMilli).atZone(zoneId).toLocalDateTime())

val encoded = encoder.encode(key)(sub = sub, exp = exp, iat = iat)(payload)

```

You can go to `https://jwt.io/` to test the generated token

# Decoding

```scala 3
trait JWTDecoder[F[*], P]:
  def decode(privateKey: String)(accessToken: String): F[(Claim, P)]
```

Decoding is the act of converting (a coded message/information) into an intelligible language. In our case convert a String with strange letters and symbols into a understandable case class

When decoding a given JWT(JSON Web Token) you can fall into 5 possible errors:

- **Token does not contain at least 2 parts** `The token must have at least 2 parts header.payload if yout don't want to validate it. If there is more or less than 2 it is considered invalid`
- **Token does not contain exact 3 parts** `The token must have 3 parts header.payload.signature if yout want full validation. If there is more or less than 3 it is considered invalid`
- **Invalid Signature** `It might be a fraud or a mistake, so the signature might not match`
- **Expired Token** `The token might have an exp field indicating when it will expire and by the time you are decoding it, it might be already expired`
- **Payload Decoding failure** `During the parsing of the payload some error may occur like bad payload format`

You can instantiate a JWTDecoder[F, P] by five ways:

- `def noValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Decoder: Base64Decoder[F]): JWTDecoder[F, P]`: This instance provides no validation towards the token. It only extracts the Payload and the claim(considering they're valid json values). Recommended for tests purposes 
- `def useHeaderNoExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F],base64Decoder: Base64Decoder[F]): JWTDecoder[F, P]`: This instance checks the header to find out the algorithm used to sign the token then proceeds to validate the signature but does not check for expiration date   
- `def noExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F],base64Decoder: Base64Decoder[F],encodeAlg: HmacEncoderAlgorithms): JWTDecoder[F, P]`: This instance ignores the header and uses the algEncoder provided to validate the signature but does not check for expiration date
- `def useHeaderAllValidations[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F],base64Decoder: Base64Decoder[F])(using ZoneId): JWTDecoder[F, P]`: This instance checks the header to find out the algorithm used to sign the token then proceeds to validate the signature and checks for expiration date
- `def allValidations[F[*]: [F[*]] =>> MonadError[F, Throwable], P: Codec](base64Encoder: Base64Encoder[F],base64Decoder: Base64Decoder[F],encodeAlg: HmacEncoderAlgorithms)(using ZoneId): JWTDecoder[F, P]`: This instance ignores the header and uses the algEncoder provided to validate the signature and checks for expiration date
- **P** It is the type of the class you want to decode after the JWT is completely decoded and is valid. It needs a io.Circe.Codec instance for the JWTDecoder be able to convert it from a JSON String
- **ZoneId** An implicit instance of java.time.ZoneId object to help converting iat from Epoch milli to java.time.LocalDateTime in a way that you can control the time zone

Once instantiated this is the only method JWTDecoder has:

- `def decode(privateKey: String)(accessToken: String): F[(Claim, P)]`:

  - **privateKey: String** This is the key that will be used to check weather the provided signature matches with the actual encryption of the header and payload

  - **token: String** The token that will be decoded and in case it succeeds its payload and the claim will be returned

Example: Using the header to figure out the hmac algorithm

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

given zoneId: ZoneId = ZoneId.of("UTC")

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val decoder: JWTDecoder[F, Payload] =
  JWTDecoder.useHeaderAllValidations(base64Encoder, base64Decoder)

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[(Claim, Payload)] = decoder.decode(key)(input)

```

Example: Ignoring the header and providing the Hmac instance yourself

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import java.time.ZoneId

import io.github.kiberStender.fjwt.crypto.base64.Base64Decoder
import io.github.kiberStender.fjwt.crypto.hs.HmacEncoder
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

final case class Payload(name: String, admin: Boolean)

given Decoder[Payload] = io.circe.generic.semiauto.deriveCodec

given zoneId: ZoneId = ZoneId.of("UTC")

type F = [T] =>> Either[Throwable, T]
lazy val base64Encoder: Base64Encoder[F] = Base64Encoder.dsl
lazy val base64Decoder: Base64Decoder[F] = Base64Decoder.dsl
lazy val hmacEncoder: HmacEncoderAlgorithms = HmacSHA512
lazy val decoder: JWTDecoder[F, Payload] =
  JWTDecoder.allValidations(base64Encoder, base64Decoder, hmacEncoder)

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[(Claim, Payload)] = decoder.decode(key)(input)

```

# Using the library

In order to use this library just add it to your build dependency list 
```scala
libraryDependencies += "io.github.kiberStender" %% "fjwt" % "1.0.3"
```