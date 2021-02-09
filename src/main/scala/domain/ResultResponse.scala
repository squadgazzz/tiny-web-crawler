package domain

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.EntityEncoder
import org.http4s.circe._

case class ResultResponse(titles: Seq[TitleWithUrl])

object ResultResponse {
  implicit val encoder: EntityEncoder[IO, ResultResponse] = jsonEncoderOf[IO, ResultResponse]
}
