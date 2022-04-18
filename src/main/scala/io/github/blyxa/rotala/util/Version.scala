package io.github.blyxa.rotala.util

import java.time.{Instant, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter

object Version{
  def fetch():Version = {
    val formatter: DateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss Z")
      .withZone(ZoneId.from(ZoneOffset.UTC))
    val mp = new MainProperties("version-rotala.txt", false)

    val numer:String = mp.getProperty("number","none")
    val buildTimeEpoch:Long = mp.getProperty("build-time-epoch","0").toLong
    val buildTime:String = formatter.format(Instant.ofEpochMilli(buildTimeEpoch))
    Version(numer, buildTimeEpoch, buildTime)
  }
}
case class Version(number:String, buildTimeEpochMs:Long, buildTimeString:String)