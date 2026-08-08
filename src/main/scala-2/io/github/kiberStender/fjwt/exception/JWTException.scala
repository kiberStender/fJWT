package io.github.kiberStender
package fjwt
package exception

/** Enum to delimit and map all possible errors that the API can "throw"
  */
sealed trait JWTException extends Throwable {
  def message: String
}

object JWTException {
  case object ExpiredTokenException extends JWTException {
    val message = "Token is expired"
  }

  case class NotMappedException(message: String) extends JWTException

  case object NullPrivateKeyException extends JWTException {
    val message = "Private key cannot null"
  }

  case object EmptyPrivateKeyException extends JWTException {
    val message = "Private key cannot be empty"
  }

  case object InvalidSignatureException extends JWTException {
    val message = "JWT Signature does not match"
  }

  case object NullTokenException extends JWTException {
    val message = "Token cannot null"
  }

  case object EmptyTokenException extends JWTException {
    val message = "Token cannot be empty"
  }

  case object Not2TokenPartsException extends JWTException {
    val message = "Token has less than 2 parts: header.payload"
  }
  case object Not3TokenPartsException extends JWTException {
    val message = "Token has less than 3 parts: header.payload.signature"
  }

  case class InvalidAlgException(alg: String) extends JWTException {
    val message = s"Provided alg is invalid or not supported: [$alg]"
  }
}
