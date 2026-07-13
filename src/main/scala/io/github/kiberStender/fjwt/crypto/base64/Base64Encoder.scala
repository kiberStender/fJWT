package io.github.kiberStender
package fjwt
package crypto
package base64

/** A trait describing a [[Base64Encoder]]
  * @tparam F
  *   A given container that wraps the return type
  */
trait Base64Encoder[F[*]] {

  /** A method to encode a given string using Base64 algorithm
    *
    * @param str
    *   The String to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encode(str: String): F[String] = encode(str.toBytesUTF8)

  /** A method to encode a given string using Base64 algorithm
    *
    * @param data`
    *   The [[Array]][[Byte]] to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encode(data: Array[Byte]): F[String]

  /** A method to encode a given string using Base64 URL safe algorithm
    * @param str
    *   The String to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encodeURLSafe(str: String): F[String] = encodeURLSafe(str.toBytesUTF8)

  /** A method to encode a given string using Base64 URL safe algorithm
    *
    * @param data
    *   The [[Array]][[Byte]] to be encoded in Base64 algorithm
    * @return
    *   A String encoded in Base64 algorithm wrapped in F
    */
  def encodeURLSafe(data: Array[Byte]): F[String]
}
