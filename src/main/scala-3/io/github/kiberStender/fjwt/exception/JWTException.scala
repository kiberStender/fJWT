package io.github.kiberStender
package fjwt
package exception

/** Enum to delimit and map all possible errors that the API can "throw"
  */
enum JWTException(val message: String) extends Throwable(message):
  case NotMappedException(errorMessage: String) extends JWTException(errorMessage)
  case ExpiredTokenException extends JWTException("Token is expired")
  case InvalidSignatureException extends JWTException("JWT Signature does not match")
  case NullPrivateKeyException extends JWTException("Private key cannot null")
  case EmptyPrivateKeyException extends JWTException("Private key cannot be empty")
  case NullTokenException extends JWTException("Token cannot null")
  case EmptyTokenException extends JWTException("Token cannot be empty")
  case Not2TokenPartsException extends JWTException("Token has less than 2 parts: header.payload")
  case Not3TokenPartsException
      extends JWTException("Token has less than 3 parts: header.payload.signature")
  case InvalidAlgException(alg: String)
      extends JWTException(s"Provided alg is invalid or not supported: [$alg]")
