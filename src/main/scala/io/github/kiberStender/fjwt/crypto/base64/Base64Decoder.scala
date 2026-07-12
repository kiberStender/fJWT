package io.github.kiberStender
package fjwt
package crypto
package base64

/** A trait describing A [[Base64Decoder]]
  * @tparam F
  *   A given container that wraps the return type
  */
trait Base64Decoder[F[*]] {

  /** Method to decode a string previously encoded in Base64
    * @param str
    *   The String to be decoded using Base64 algorithm
    * @return
    *   A String that was decoded from Base64 algorithm wrapped in F
    */
  def decode(str: String): F[String]
}
