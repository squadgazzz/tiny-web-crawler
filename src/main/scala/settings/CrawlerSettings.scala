package settings

import cats.Show
import pureconfig._
import pureconfig.generic.semiauto.deriveReader

import scala.concurrent.duration.FiniteDuration

case class CrawlerSettings(
  hostname: String,
  port: Int,
  requestTimeout: FiniteDuration,
  connectionTimeout: FiniteDuration,
  webPageLoadLimit: Int,
  cacheLimit: Int)

object CrawlerSettings {
  implicit val configReader: ConfigReader[CrawlerSettings] = deriveReader

  implicit val toPrintable: Show[CrawlerSettings] = {
    case s: CrawlerSettings =>
      s"""
         |hostname: ${s.hostname}
         |port: ${s.port}
         |requestTimeout: ${s.requestTimeout}
         |connectionTimeout: ${s.connectionTimeout}
         |webPageLoadLimit: ${s.webPageLoadLimit}
         |cacheLimit: ${s.cacheLimit}
       """.stripMargin
  }
}
