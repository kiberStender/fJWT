package io.github.kiberStender
package fjwt
package json

/** A trait used to generify JSON parsing, so to not force the user to use a
  * specific json library
  * @tparam F
  *   The effect type
  * @tparam J
  *   The JSON object to be parsed from the given String
  */
trait JsonDecoder[F[*], J] {

  /** The method to effectively parse the given String to the type J
    * @param json
    *   The JSON formatted string to be parsed to J
    * @return
    *   Either a J instance or an error that might be thrown
    */
  def decode(json: String): F[J]
}

/** A trait used to generify JSON conversion, so to not force the user to use a
  * specific json library
  * @tparam F
  *   The effect type
  * @tparam J
  *   The object to be converted to a JSON string format
  */
trait JsonEncoder[F[*], J] {

  /** The method to effectively convert the given T instance to a JSON formatted
    * String
    * @param json
    *   The object to be converted to a JSON string format
    * @return
    *   Either a JSON String formatted or whatever error it might throw
    */
  def encode(json: J): F[String]
}
