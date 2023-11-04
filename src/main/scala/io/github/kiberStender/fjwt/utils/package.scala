package io.github.kiberStender
package fjwt

import java.nio.charset.StandardCharsets
import java.time.{LocalDateTime, ZoneId}

package object utils:
  extension (zoneId: ZoneId) {

    /** Method to simplify getting eppoc time from a zone id
      * @param ldt
      *   The {@link LocalDateTime} object to be have epoch time extracted
      * @return
      *   A {@link Long} value containing the date and time formatted in epoch
      */
    def toEpoch(ldt: LocalDateTime): Long = ldt.atZone(zoneId).toInstant.toEpochMilli
  }

  extension (str: String) {
    def toBytesUTF8: Array[Byte] = str getBytes StandardCharsets.UTF_8
  }
