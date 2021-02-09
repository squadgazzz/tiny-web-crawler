package http.sync

import cats.data.EitherT
import cats.effect.IO
import domain.UrlsRequest
import http.ApiRoute
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.io._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{->, InternalServerError, Ok, POST, Root}
import service.CrawlingService

case class SyncCrawlerApiRoute(crawlingService: CrawlingService) extends ApiRoute {
  private val syncService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "sync" =>
      (for {
        req      <- EitherT.right(req.as[UrlsRequest])
        result   <- EitherT(crawlingService.processUrls(req.urls))
        response <- EitherT.rightT[IO, String](result.asJson)
      } yield response).foldF(InternalServerError(_), Ok(_))
  }

  val routes: HttpRoutes[IO] = syncService
}
