import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.concurrent.SignallingRef
import http.async.AsyncCrawlerApiRoute
import http.sync.SyncCrawlerApiRoute
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource
import service.{CrawlingService, UrlConnector}
import settings.CrawlerSettings
import utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp with Logging {
  implicit val cs: ContextShift[IO]      = IO.contextShift(global)
  override implicit val timer: Timer[IO] = IO.timer(global)

  val settings: CrawlerSettings         = ConfigSource.default.loadOrThrow[CrawlerSettings]
  val urlConnector: UrlConnector        = UrlConnector(settings)
  val service: CrawlingService          = CrawlingService(urlConnector)
  val syncRoutes: SyncCrawlerApiRoute   = SyncCrawlerApiRoute(service)
  val asyncRoutes: AsyncCrawlerApiRoute = AsyncCrawlerApiRoute(service)
  val routes: HttpRoutes[IO]            = syncRoutes.routes <+> asyncRoutes.routes

  override def run(args: List[String]): IO[ExitCode] = {
    log.info(show"Configuration: $settings")
    (for {
      signal   <- fs2.Stream.eval(SignallingRef[IO, Boolean](false))
      exitCode <- fs2.Stream.eval(Ref[IO].of(ExitCode.Success))
      _        <- fs2.Stream.eval(IO(sys.addShutdownHook(signal.set(true))))
      server <- BlazeServerBuilder[IO](global)
        .bindHttp(settings.port, settings.hostname)
        .withHttpApp(routes.orNotFound)
        .serveWhile(signal, exitCode)
    } yield server).compile.drain.as(ExitCode.Success)
  }
}
