package domain

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe._

case class UrlsRequest(urls: Seq[String])

object UrlsRequest {
  implicit val decoder: EntityDecoder[IO, UrlsRequest] = jsonOf[IO, UrlsRequest]
}
