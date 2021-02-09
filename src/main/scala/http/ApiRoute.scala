package http

import cats.effect.IO
import org.http4s.HttpRoutes

trait ApiRoute {
  def routes: HttpRoutes[IO]
}
