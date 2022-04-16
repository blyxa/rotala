package io.github.blyxa.rotala.util

import java.time.{Instant, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter

class Version{
  private val formatter: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss Z")
    .withZone(ZoneId.from(ZoneOffset.UTC))
  private val mp = new MainProperties("version-rotala.txt", false)

  val numer:String = mp.getProperty("number","none")
  val buildTimeEpoch:Long = mp.getProperty("build-time-epoch","0").toLong
  val buildTime:String = formatter.format(Instant.ofEpochMilli(buildTimeEpoch))

  override def toString: String =  s"number[$numer] buildTime[$buildTime]"

}
