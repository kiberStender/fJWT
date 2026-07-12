# F[JWT]

Simple Scala 2 and 3 library to generate and validate JWT`s (JSON Web Tokens) written in Tagless Final style

For a given hypothetical class Payload that's how one encode and decode it

```scala
final case class Payload(name: String, admin: Boolean)
```

# Encoding

Scala 2
```scala
trait JWTEncoder[F[*], T, P] {
  def encode(privateKey: String)(claim: Claim[T])(payload: P): F[String]
}
```
Scala 3
```scala 3
trait JWTEncoder[F[*], T, P]:
  def encode(privateKey: String)(claim: Claim[T])(payload: P): F[String]
```

Encoding is the act of converting a given information into a particular form(in this case a 3 parts string). 

As this is a 3 part string, each part is prone to possible errors, like failing to parse to json, you might forget to provide the private key as it usually comes from some config file, environment variable, etc.
Therefore the Encoding type is `MonadError[F]`. This type guarantees you won't lose any errors that might come, and you will be able to treat them accordingly(See example below)

In order to instantiate a JWTEncoder to start generating your JWTs(JSON Web Token) using the provided factory method, you will need the following:
- **Base64Encoder[F]** You can either use the already provided Base64Encoder(which is a simple wrapper around `org.apache.commons.codec.binary.Base64` class) in the package package `io.github.kiberStender.fjwt.implicits.base64` or create your own instance using any other library you prefer  

- **Hmac[F]** You can either use the already provided Hmac(which is a simple wrapper around `org.apache.commons.codec.digest.HmacUtils` class) in the package `io.github.kiberStender.fjwt.implicits.header` or you can provide your own instance using any other library you prefer

- **ToLong[T]** It is the type of time measurement you want to use. It is generic to be flexible to either user any library you want(Joda Time, Java LocalDateTime library, etc) or your own implementation like a simple Long or whatever you need at the moment. In order for it to properly work, you only need to implement the trait ToLong, which is just you explaining how to convert your custom Time type to Long(as per JWT convention, all time based fields must be a Long number when converted to json)

- **JsonEncoder[P]** In order to make this library customisable enough, you have to create your own instance of JsonEncoder[P] to provide a way to parse your payload to Json, without me forcing you to chose a given library

- **HmacAlgorithm** Used to tell which algorithm you want to use in order to create the third part of the token, the token signature

After providing these dependencies(as implicit values) you can easily create an instance of JWTEncoder by calling the method:
```scala
def dsl[F[*]: MonadError[*[_], Throwable]: Base64Encoder: Hmac: ToLong[*[*], T]: JsonEncoder[*[*], P], T, P](hmacAlg: HmacAlgorithm): JWTEncoder[F, T, P]
```
```scala 3
def dsl[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Encoder: Hmac: [F[*]] =>> ToLong[F, T]: [F[*]] =>> JsonEncoder[F, P], T, P](hmacAlg: HmacAlgorithm): JWTEncoder[F, T, P]
``` 
 
And by providing any instance of MonadError[F] as cited above. 

After you have your instance of JWTEncoder, you can create your JWT Token by using `encode` method:

```scala
def encode(privateKey: String)(claim: Claim[T])(payload: P): F[String]
```

- **privateKey: String** This is the key that will be used to encrypt both your header and payload to generate the signature of your JWT

- **claim Claim[Time]** The object that holds the "metadata" of your token
  - **iss: Option[String]** Optional issuer

  - **sub: Option[String]** Optional subject

  - **aud: Option[String]** Optional intended audience

  - **exp: Option[Time]** Optional expiration time

  - **nbf: Option[Time]** Optional not before time

  - **iat: Option[Time]** Optional issued at time

  - **jti: Option[String]** Optional JWT ID

- **payload: P** It is the payload itself. An instance of P that you have to provide

Example with simple Long value in Scala 2:

```scala
import cats._, cats.syntax.all._
import io.circe._

import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512

final case class Payload(name: String, admin: Boolean)

object Payload{
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec
  
  private implicit val payloadCirceCodec: Codec[Payload] = deriveCodec
  
  implicit def jsonEncoderPayload[F[*]: ApplicativeError[*[_], Throwable]]: JsonEncoder[F, Payload] = new JsonEncoder[F, Payload] {
    def encode(payload: Payload): F[String] = payload.asJson.noSpaces.pure[F]
    }
}

type F[T] = Either[Throwable, T]

// Bas64Encoder implicit instance using the apache commons library implementation
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Hmac implicit for hashing / signing the token using apache commons library implementation
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
// An instance of the trait ToLong, explaining how to convert a Long value to a Long value(redudant example, I know) 
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.toLong
// JsonEncoder[Payload] instace for for parsing a given Payload object into json (In this example using Circe library)
import Payload._

val encoder: JWTEncoder[F, Long, Payload] = JWTEncoder.dsl(hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = "1234567890".some
val iat: Option[Long] = 1516239022.some
val exp: Option[Long] = 1516239150.some
val claim: Claim[Long] = Claim(sub = sub, iat = iat, exp = exp)

val encoded: F[String] = encoder.encode(privateKey)(claim)(payload)
```

The same example with simple Long value in Scala 3:

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512

final case class Payload(name: String, admin: Boolean)

object Payload:
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private given Codec[Payload] = deriveCodec

  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonEncoder[Payload] with  
    def encode(payload: Payload): F[String] = payload.asJson.noSpaces.pure[F]

type F = [F] =>> Either[Throwable, T]

// Bas64Encoder implicit instance using the apache commons library implementation
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Hmac implicit for hashing / signing the token using apache commons library implementation
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
// An instance of the trait ToLong, explaining how to convert a Long value to a Long value(redudant example, I know) 
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.toLong
// JsonEncoder[Payload] instace for for parsing a given Payload object into json using Circe library
import Payload.*

val encoder: JWTEncoder[F, Claim[Long], Payload] = JWTEncoder.dsl(hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = "1234567890".some
val iat: Option[Long] = 1516239022.some
val exp: Option[Long] = 1516239150.some
val claim: Claim[Long] = Claim(sub = sub, iat = iat, exp = exp)

val encoded: F[String] = encoder.encode(privateKey)(claim)(payload)
```

Example using java.time.LocalDateTime value in Scala 2:

```scala
import cats._, cats.syntax.all._
import io.circe._

import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512

import java.time.LocalDateTime

final case class Payload(name: String, admin: Boolean)

object Payload{
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec
  
  private implicit val payloadCirceCodec: Codec[Payload] = deriveCodec
  
  implicit def jsonEncoderPayload[F[*]: ApplicativeError[*[_], Throwable]]: JsonEncoder[Payload] = new JsonEncoder[Payload] {
    def encode(payload: Payload): F[String] = payload.asJson.noSpaces.pure[F]
    }
}

type F[T] = Either[Throwable, T]

// Bas64Encoder implicit instance using the apache commons library implementation
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Hmac implicit for hashing / signing the token using apache commons library implementation
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
// An instance of the trait ToLong, explaining how to convert a LocalDateTime value to a Long value 
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.toLong
// JsonEncoder[Payload] instace for for parsing a given Payload object into json (In this example using Circe library)
import Payload._

val encoder: JWTEncoder[F, LocalDateTime, Payload] = JWTEncoder.dsl(hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = "1234567890".some
val now = LocalDateTime().now()
val iat: Option[Long] = now.minusDays(3).some
val exp: Option[Long] = now.minusDays(2).some
val claim: Claim[LocalDateTime] = Claim(sub = sub, iat = iat, exp = exp)

val encoded: F[String] = encoder.encode(privateKey)(claim)(payload)
```

The same example using java.time.LocalDateTime value in Scala 3:

```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.models.Claim
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm
import io.github.kiberStender.fjwt.models.crypto.HmacAlgorithm.HmacSHA512

import java.time.LocalDateTime

final case class Payload(name: String, admin: Boolean)

object Payload:
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private given Codec[Payload] = deriveCodec

  given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonEncoder[Payload] with  
    def encode(payload: Payload): F[String] = payload.asJson.noSpaces.pure[F]

type F = [F] =>> Either[Throwable, T]

// Bas64Encoder implicit instance using the apache commons library implementation
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Hmac implicit for hashing / signing the token using apache commons library implementation
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons
// An instance of the trait ToLong, explaining how to convert a LocalDateTime value to a Long value 
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LocalDateTimeInstances.toLong
// JsonEncoder[Payload] instace for for parsing a given Payload object into json using Circe library
import Payload.*

val encoder: JWTEncoder[F, LocalDateTime, Payload] = JWTEncoder.dsl(hs512Encoder)

val payload: Payload = Payload("John Doe", true)
val privateKey: String = "a-super-secret-key"
val sub: Option[String] = "1234567890".some
val now = LocalDateTime().now()
val iat: Option[Long] = now.minusDays(3).some
val exp: Option[Long] = now.minusDays(2).some
val claim: Claim[LocalDateTime] = Claim(sub = sub, iat = iat, exp = exp)

val encoded: F[String] = encoder.encode(privateKey)(claim)(payload)
```

You can go to `https://jwt.io/` to test the generated token

# Decoding

```scala
trait JWTDecoder[F[*], T, P] {
  def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]]
}
```

```scala 3
trait JWTDecoder[F[*], P]:
  def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]]
```

Decoding is the act of converting (a coded message/information) into an intelligible language. In our case convert a String with strange letters and symbols into a understandable case class

When decoding a given JWT(JSON Web Token) you can fall into 5 possible errors:

- **Token does not contain at least 2 parts** `The token must have at least 2 parts header.payload if yout don't want to validate it. If there is more or less than 2 it is considered invalid`
- **Token does not contain exact 3 parts** `The token must have 3 parts header.payload.signature if yout want full validation. If there is more or less than 3 it is considered invalid`
- **Invalid Signature** `It might be a fraud or a mistake, so the signature might not match`
- **Expired Token** `The token might have an exp field indicating when it will expire and by the time you are decoding it, it might be already expired`
- **Payload Decoding failure** `During the parsing of the payload some error may occur like bad payload format`

You can instantiate a JWTDecoder[F, P] by five ways:

### noValidation

This instance provides no validation towards the token. 

It only extracts the Payload and the claim(considering they're valid json values). 

Recommended for tests purposes only

- `def noValidation[F[*]: MonadError[*[_],Throwable]: Base64Decoder, T: FromLong, P: JsonDecoder]: JWTDecoder[F, T, P]`: (Scala 2)
- `def noValidation[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Decoder: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P]: JWTDecoder[F, T, P]`: (Scala 3)

1. **Base64Decoder[F]**: An implicit instance of the trait `Base64Decoder` that implements how to encrypt a given string into a base64 text
1. **T**: The type of the time used for the case class after parsing the json's payload
1. **FromLong[F, T]**: An implicit instance of the trait `FromLong` that "teaches" how to convert a Long to whatever `T` is
1. **P**: The type of the Payload, after the second part of the token is parsed without the claim metadata values 
1. **JsonDecoder[F, P]**: An implicit instance of how to parse a json string payload into the type P.

### useHeaderNoExpirationValidation

This instance checks the header to find out the algorithm used to sign the token then proceeds to validate the signature but does not check for 
expiration date

Recommended for tests purposes or if you want to check expiration at a later moment

- `def useHeaderNoExpirationValidation[F[*]: MonadError[*[_],Throwable]: Base64Encoder: Base64Decoder: Hmac, T: FromLong, P: JsonDecoder]: JWTDecoder[F, T, P]`: (Scala 2)
- `def useHeaderNoExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P]: JWTDecoder[F, T, P]`: (Scala 3)

1. **Base64Encoder[F]**: An implicit instance of the trait `Base64Encoder` that implements how to decrypt a given base64 text into a string
1. **Base64Decoder[F]**: An implicit instance of the trait `Base64Decoder` that implements how to encrypt a given string into a base64 text
1. **Hmac[F]**: An implicit instance of the trait `Hmac` that implements the algorithms used to sign the token
1. **T**: The type of the time used for the case class after parsing the json's payload
1. **FromLong[F, T]**: An implicit instance of the trait `FromLong` that "teaches" how to convert a Long to whatever `T` is
1. **P**: The type of the Payload, after the second part of the token is parsed without the claim metadata values
1. **JsonDecoder[F, P]**: An implicit instance of how to parse a json string payload into the type P.

### noExpirationValidation

This instance ignores the header and uses the algEncoder provided to validate the signature but does not check for expiration date

Faster than `useHeaderNoExpirationValidation` as it skips parsing the header to find out the algorithm used to sign the token. Use only if you know
the algorithm used

Recommended for tests purposes or if you want to check expiration at a later moment

- `def noExpirationValidation[F[*]: MonadError[*[_],Throwable]: Base64Encoder: Base64Decoder: Hmac, T: FromLong, P: JsonDecoder](encodeAlg: HmacAlgorithm): JWTDecoder[F, T, P]`: (Scala 2)
- `def noExpirationValidation[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P](encodeAlg: HmacAlgorithm): JWTDecoder[F, T, P]`: (Scala 3)

1. **Base64Encoder[F]**: An implicit instance of the trait `Base64Encoder` that implements how to decrypt a given base64 text into a string
1. **Base64Decoder[F]**: An implicit instance of the trait `Base64Decoder` that implements how to encrypt a given string into a base64 text
1. **Hmac[F]**: An implicit instance of the trait `Hmac` that implements the algorithms used to sign the token
1. **T**: The type of the time used for the case class after parsing the json's payload
1. **FromLong[F, T]**: An implicit instance of the trait `FromLong` that "teaches" how to convert a Long to whatever `T` is
1. **P**: The type of the Payload, after the second part of the token is parsed without the claim metadata values
1. **JsonDecoder[F, P]**: An implicit instance of how to parse a json string payload into the type P.
1. **encodeAlg: HmacAlgorithm**: The algorithm used to sign the token. Used to be faster when decoding the token

### useHeaderAllValidations

This instance checks the header to find out the algorithm used to sign the token then proceeds to validate the signature and checks for 
expiration date

Recommend for production

- `def useHeaderAllValidations[F[*]: MonadError[*[_],Throwable]: Base64Encoder: Base64Decoder: Hmac, T: FromLong: Expirable, P: JsonDecoder]: JWTDecoder[F, T, P]`: (Scala 2)
- `def useHeaderAllValidations[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> Expirable[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P]: JWTDecoder[F, T, P]`: (Scala 3)

1. **Base64Encoder[F]**: An implicit instance of the trait `Base64Encoder` that implements how to decrypt a given base64 text into a string
1. **Base64Decoder[F]**: An implicit instance of the trait `Base64Decoder` that implements how to encrypt a given string into a base64 text
1. **Hmac[F]**: An implicit instance of the trait `Hmac` that implements the algorithms used to sign the token
1. **T**: The type of the time used for the case class after parsing the json's payload
1. **FromLong[F, T]**: An implicit instance of the trait `FromLong` that "teaches" how to convert a Long to whatever `T` is
1. **Expirable[F, T]**: An implicit instance of the trait `Expirable` that "teaches" how to check if the given token is expired
1. **P**: The type of the Payload, after the second part of the token is parsed without the claim metadata values
1. **JsonDecoder[F, P]**: An implicit instance of how to parse a json string payload into the type P.

### allValidations

This instance ignores the header and uses the algEncoder provided to validate the signature and checks for expiration date

Faster than `useHeaderAllValidations` as it skips parsing the header to find out the algorithm used to sign the token. Use only if you know
the algorithm used

Recommend for production

- `def allValidations[F[*]: MonadError[*[_],Throwable]: Base64Encoder: Base64Decoder: Hmac, T: FromLong: Expirable, P: JsonDecoder](encodeAlg: HmacAlgorithm): JWTDecoder[F, T, P]`: (Scala 2)
- `def allValidations[F[*]: [F[*]] =>> MonadError[F, Throwable]: Base64Encoder: Base64Decoder: Hmac: [F[*]] =>> FromLong[F, T]: [F[*]] =>> Expirable[F, T]: [F[*]] =>> JsonDecoder[F, P], T, P](encodeAlg: HmacAlgorithm): JWTDecoder[F, T, P]`: (Scala 3)

1. **Base64Encoder[F]**: An implicit instance of the trait `Base64Encoder` that implements how to decrypt a given base64 text into a string
1. **Base64Decoder[F]**: An implicit instance of the trait `Base64Decoder` that implements how to encrypt a given string into a base64 text
1. **Hmac[F]**: An implicit instance of the trait `Hmac` that implements the algorithms used to sign the token
1. **T**: The type of the time used for the case class after parsing the json's payload
1. **FromLong[F, T]**: An implicit instance of the trait `FromLong` that "teaches" how to convert a Long to whatever `T` is
1. **Expirable[F, T]**: An implicit instance of the trait `Expirable` that "teaches" how to check if the given token is expired
1. **P**: The type of the Payload, after the second part of the token is parsed without the claim metadata values
1. **JsonDecoder[F, P]**: An implicit instance of how to parse a json string payload into the type P.
1. **encodeAlg: HmacAlgorithm**: The algorithm used to sign the token. Used to be faster when decoding the token

Once instantiated this is the only method JWTDecoder has:

- `def decode(privateKey: String)(accessToken: String): F[JWToken[T, P]]`:

    - **privateKey: String** This is the key that will be used to check weather the provided signature matches with the actual encryption of the header and payload

    - **accessToken: String** The token that will be decoded. In case it succeeds an instance of the case class JWToken[T, P] is returned

Example: Using the header to figure out the hmac algorithm

Scala 2:
```scala
import cats._, cats.syntax.all._
import io.circe._

import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

object Payload {
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private implicit val pCodec: Codec[Payload] = deriveCodec

  implicit def pJsondecoder[F[*] : ApplicativeError[*[_], Throwable]]: JsonDecoder[F, Payload] = new JsonDecoder[F, Payload] {
    def decode(json: String): F[Payload] = parser.decode[Payload](json) match {
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]
    }
  }
}

type F[T] = Either[Throwable, T]
given zoneId: ZoneId = ZoneId.of("UTC")

// Implicit instance for Base64Encoder, used to validate the token
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Implicit instance for Base64Decoder, used to decrypt the data before parsing
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
// Implicit instance for FromLong, teaching how to convert Long(the date and time format in the token) to T 
// and an instance to Expirable "teaching" how to calculate expiration
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLong, expirable}
// Implicit instance to Hmac, used to check if the signature is valid
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.useHeaderAllValidations

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[JWToken[Long, Payload]] = decoder.decode(key)(input)
```

Scala 3:
```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

object Payload:
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private given pCodec: Codec[Payload] = deriveCodec

  given [F[*] : ApplicativeError[*[_], Throwable]]: JsonDecoder[F, Payload] with
    def decode(json: String): F[Payload] = parser decode[Payload] json match
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]

type F = [T] =>> Either[Throwable, T]
given zoneId: ZoneId = ZoneId.of("UTC")

// Implicit instance for Base64Encoder, used to validate the token
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Implicit instance for Base64Decoder, used to decrypt the data before parsing
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
// Implicit instance for FromLong, teaching how to convert Long(the date and time format in the token) to T 
// and an instance to Expirable "teaching" how to calculate expiration
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLong, expirable}
// Implicit instance to Hmac, used to check if the signature is valid
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.useHeaderAllValidations

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[JWToken[Long, Payload]] = decoder.decode(key)(input)
```

Example: Ignoring the header and providing the Hmac instance yourself

Scala 2:
```scala
import cats._, cats.syntax.all._
import io.circe._

import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

object Payload {
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private implicit val pCodec: Codec[Payload] = deriveCodec

  implicit def pJsondecoder[F[*] : ApplicativeError[*[_], Throwable]]: JsonDecoder[F, Payload] = new JsonDecoder[F, Payload] {
    def decode(json: String): F[Payload] = parser.decode[Payload](json) match {
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]
    }
  }
}

type F[T] = Either[Throwable, T]
given zoneId: ZoneId = ZoneId.of("UTC")

// Implicit instance for Base64Encoder, used to validate the token
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Implicit instance for Base64Decoder, used to decrypt the data before parsing
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
// Implicit instance for FromLong, teaching how to convert Long(the date and time format in the token) to T 
// and an instance to Expirable "teaching" how to calculate expiration
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLong, expirable}
// Implicit instance to Hmac, used to check if the signature is valid
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.useHeaderAllValidations

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[JWToken[Long, Payload]] = decoder.decode(key)(input)
```

Scala 3:
```scala 3
import cats.*, cats.syntax.all.*
import io.circe.*

import io.github.kiberStender.fjwt.crypto.base64.{Base64Decoder, Base64Encoder}
import io.github.kiberStender.fjwt.JWTDecoder
import io.github.kiberStender.fjwt.model.Claim

import java.time.ZoneId

final case class Payload(name: String, admin: Boolean)

object Payload:
  import io.circe.{Codec, parser}
  import io.circe.generic.semiauto.deriveCodec

  private given Codec[Payload] = deriveCodec

  given [F[*] : ApplicativeError[*[_], Throwable]]: JsonDecoder[F, Payload] with
    def decode(json: String): F[Payload] = parser decode[Payload] json match
      case Left(value) => value.raiseError[F, Payload]
      case Right(value) => value.pure[F]

type F = [T] =>> Either[Throwable, T]
given zoneId: ZoneId = ZoneId.of("UTC")

// Implicit instance for Base64Encoder, used to validate the token
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonEncoder
// Implicit instance for Base64Decoder, used to decrypt the data before parsing
import io.github.kiberStender.fjwt.implicits.base64.Implicits.apacheCommonDecoder
// Implicit instance for FromLong, teaching how to convert Long(the date and time format in the token) to T 
// and an instance to Expirable "teaching" how to calculate expiration
import io.github.kiberStender.fjwt.implicits.claim.Implicits.LongInstances.{fromLong, expirable}
// Implicit instance to Hmac, used to check if the signature is valid
import io.github.kiberStender.fjwt.implicits.header.Implicits.hmacEncoderApacheCommons

val encodeAlg: HmacAlgorithm = HmacSHA512
val decoder: JWTDecoder[F, Long, Payload] = JWTDecoder.allValidations(encodeAlg)

val key = "a-super-secret-key"
val input = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOm51bGwsInN1YiI6IjEyMzQ1Njc4OTAiLCJhdWQiOm51bGwsImV4cCI6bnVsbCwibmJmIjpudWxsLCJpYXQiOjE1MTYyMzkwMjIsImp0aSI6bnVsbCwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.skzMrv6PjD9CDU-xeXKEFMAXqvYAY98_nG7SQjs6RcU2qWfKOeimd9kcdovNWoYIY6ejo1KIreElP7NRnevI2A"

val decoded: F[JWToken[Long, Payload]] = decoder.decode(key)(input)
```

# Instances provided

The core of this library are the 2 traits `JWTEncoder` and `JWTDecoder`, the rest is just dependencies that can be replaced by you, so you you're not forced
to add new libraries in your code if you already one that does what the dependencies does. You will only need to write your own instance

For Base64Encoder and Base64Decoder it is used apache commons codec("commons-codec" % "commons-codec" % "1.22.0")

If you want to make your instance, in case you have a better Base64 library you can simply

Scala 2:
```scala
implicit def apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] = new Base64Encoder[F] {
  // String version is overloaded based on this one, so you don't need to make def encode(data: String): F[String] 
  def encode(data: Array[Byte]): F[String] = // Your code here
  
  // String version is overloaded based on this one, so you don't need to make def encode(data: String): F[String]
  def encodeURLSafe(data: Array[Byte]): F[String] = // Your code here
}

implicit def apacheCommonDecoder[F[*]: ApplicativeError[*[_], Throwable]]: Base64Decoder[F] = (str: String) => // Your code here
```

Scala 3:
```scala 3
given apacheCommonEncoder[F[*]: Applicative]: Base64Encoder[F] = with
  // String version is overloaded based on this one, so you don't need to make def encode(data: String): F[String] 
  def encode(data: Array[Byte]): F[String] = // Your code here
  
  // String version is overloaded based on this one, so you don't need to make def encode(data: String): F[String]
  def encodeURLSafe(data: Array[Byte]): F[String] = // Your code here

given apacheCommonDecoder[F[*]: [F[*] =>> ApplicativeError[F, Throwable]]: Base64Decoder[F] = (str: String) => // Your code here
```

For The Hmac algorithms it is also used apache commons codec("commons-codec" % "commons-codec" % "1.22.0")

If you want to make your instance, in case you have a better Hmac library you can simply

Scala 2:
```scala
implicit def hmacEncoderApacheCommons[F[*]: ApplicativeError[*[_], Throwable]]: Hmac[F] =
    new Hmac[F] {
      def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]] = // Your code here
}
```

Scala 3:
```scala 3
given hmacEncoderApacheCommons[F[*]: [F[*]] =>> ApplicativeError[*[_], Throwable]]: Hmac[F] with 
  def hash(hmac: HmacAlgorithm)(privateKey: String)(str: String): F[Array[Byte]] = // Your code here
```

For the traits FromLong and ToLong, instances are provided for the type Long and java.time.LocalDateTime

If you want to make your instance, in case you have a better way to convert java.util.LocalDateTime to Long, or even if you are using another
time type like [Joda-time](https://www.joda.org/joda-time/) or anything else

Scala 2:
```scala
implicit def toLong[F[*]: ApplicativeError[*[*], Throwable]]: ToLong[F, T] = new ToLong[F, T] {
  def toLong(claim: Claim[T]): F[Claim[Long]] = // Your code here
}

implicit val fromLong[F[*]: ApplicativeError[*[*], Throwable]]: FromLong[T] = new FromLong[T] {
  def fromLong(claim: Claim[Long]): F[Claim[T]] = // Your code here
}
```

Scala 3:
```scala 3
given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: ToLong[T] with
  def toLong(claim: Claim[T]): F[Claim[Long]] = // Your code here

given [F[*]: [F[*]] =>> ApplicativeError[*[*], Throwable]]: FromLong[T] with
  def fromLong(claim: Claim[Long]): F[Claim[T]] = // Your code here
```

For json parsing, circe("io.circe" %% "circe-generic" % "0.14.16" and "io.circe" %% "circe-parser" % "0.14.16") is used in the tests only as both Claim[T] and the first part of the token are "parsed" using regex to keep this library as small as possible,
so you'll always have to provide your own way to parse your payload

To make your instance:

Scala 2:
```scala
implicit def jsonLongDecoderCirce[F[*]: ApplicativeError[*[*], Throwable]]: JsonDecoder[F, Payload] = new JsonDecoder[F, Payload] {
  def decode(json: String): F[Payload] = // Your code here
}

implicit def jsonLongEncoderCirce[F[*]: ApplicativeError[*[*], Throwable]]: JsonEncoder[Payload] = new JsonEncoder[Payload] {
  def encode(json: Payload): F[String] = // Your code here
}
```

Scala 3:
```scala 3
given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonDecoder[Payload] with
  def decode(json: String): F[Payload] = // Your code here

given [F[*]: [F[*]] =>> ApplicativeError[F, Throwable]]: JsonEncoder[Payload] with
  def encode(json: Payload): F[String] = // Your code here
```


# Using the library

Maven repo changed servers for Open Source apps(like this one) and I did not see the email on time to do the migration, 
so this project will not be updated on maven repo, leaving people to only have the latest version (cited below) on maven repo. 

But I'm going to start new repos here on github to publish this project once again on maven. I'm planning to split this in 2 projects: fjwt-core and fjwt-crypto

### FJWT-Core

This will have only `JWTEncoder`, `JWTDecoder`, `FromLong` and `ToLong`  implemented and every other dependency will be only traits, to keep the jar as minimal as possible.

Even `FromLong` and `ToLong` will only have two implementations(which already exist) Long and java.util.LocalDatetime, as they already come with Scala, it will not increase the jar size in a bad way

### FJWT-Crypto

Will contain the implementation for `Base64Encoder`, `Base64Decoder` and `Hmac`  using Apache Commons codec, so people can add it if they want or as mentioned, use their own library to implemented them if they feel like

In order to use the old version of this library just add it to your build dependency list(Beware, this version only works with scala 3)
```scala
libraryDependencies += "io.github.kiberStender" %% "fjwt" % "1.0.3"
```

In case you want to use this version before I'm able to publish it on maven again(I will remove these observations once I publish it) feel free to checkout this repo and build it yourself