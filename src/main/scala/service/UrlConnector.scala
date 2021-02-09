package service

import cats.effect.{ContextShift, IO}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Response, Uri}
import settings.CrawlerSettings

import scala.concurrent.ExecutionContext

case class UrlConnector(settings: CrawlerSettings)(implicit val cs: ContextShift[IO], val ex: ExecutionContext) {
  def connectTo[A](uri: Uri)(f: Response[IO] => IO[Either[String, A]]): IO[Either[String, A]] = {
    BlazeClientBuilder[IO](implicitly)
      .withConnectTimeout(settings.connectionTimeout)
      .withRequestTimeout(settings.requestTimeout)
      .resource
      .use { client =>
        client
          .get(uri)(f)
          .handleErrorWith(err => IO.pure(Left(err.getMessage)))
      }
  }
}
