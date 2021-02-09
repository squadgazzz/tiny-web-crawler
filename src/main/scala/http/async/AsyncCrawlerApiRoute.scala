package http.async

import cats.data.EitherT
import cats.effect.IO
import domain.UrlsRequest
import http.ApiRoute
import org.http4s.circe._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.io.{->, InternalServerError, Ok, POST, Root}
import service.CrawlingService

import java.util.UUID
import scala.util.Try

case class AsyncCrawlerApiRoute(crawlingService: CrawlingService) extends ApiRoute {
  def uuidEncoder: EntityEncoder[IO, UUID] = jsonEncoderOf[IO, UUID]
  def uuidDecoder: EntityDecoder[IO, UUID] = jsonOf[IO, UUID]

  private val asyncService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "async" / uuidStr =>
      (for {
        uuid <- EitherT.fromEither[IO](extractUUID(uuidStr))
        r    <- EitherT(crawlingService.findInCache(uuid))
      } yield r).foldF(InternalServerError(_), Ok(_))
    case req @ POST -> Root / "async" =>
      for {
        req      <- req.as[UrlsRequest]
        uuid     <- crawlingService.processUrlsAsync(req.urls)
        response <- Ok(uuid.asJson)
      } yield response
  }

  private def extractUUID(str: String): Either[String, UUID] =
    Try {
      UUID.fromString(str)
    }.fold(err => Left(err.getMessage), Right(_))

  val routes: HttpRoutes[IO] = asyncService
}
