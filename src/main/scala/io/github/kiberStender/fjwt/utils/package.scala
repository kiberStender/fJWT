package io.github.kiberStender
package fjwt

import java.time.{LocalDateTime, ZoneId}

package object utils:
  extension (zoneId: ZoneId) {
    /**
     * Method to simplify getting eppoc time from a zone id
     * @param ldt The {@link LocalDateTime} object to be have epoch time extracted
     * @return A {@link Long} value containing the date and time formatted in epoch
     */
    def toEpoch(ldt: LocalDateTime): Long = ldt.atZone(zoneId).toInstant.toEpochMilli
  }
