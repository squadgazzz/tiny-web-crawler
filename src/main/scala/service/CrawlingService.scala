package service

import cats.data.EitherT
import cats.implicits._
import cats.effect.{ContextShift, IO}
import com.google.common.cache.{Cache, CacheBuilder}
import domain.{ResultResponse, TitleWithUrl}
import fs2.{text, Chunk, Stream}
import org.http4s.{Response, Uri}

import java.util.UUID

case class CrawlingService(urlConnector: UrlConnector)(implicit val cs: ContextShift[IO]) {
  private val cache: Cache[UUID, Either[String, ResultResponse]] =
    CacheBuilder
      .newBuilder()
      .maximumSize(urlConnector.settings.cacheLimit)
      .build()

  def processUrls(urls: Seq[String]): IO[Either[String, ResultResponse]] = {
    urls.toList.parUnorderedTraverse { url =>
      (for {
        uri   <- EitherT.fromEither[IO](Uri.fromString(url).leftMap(_ => s"Unable to parse URL from string '$url'"))
        title <- EitherT(urlConnector.connectTo(uri)(findTitle(_, url)))
      } yield TitleWithUrl(title, url)).value
    }.map(_.sequence.map(ResultResponse(_)))
  }

  def processUrlsAsync(urls: Seq[String]): IO[UUID] = {
    val uuid = UUID.randomUUID()

    (for {
      _      <- IO.shift
      result <- processUrls(urls)
    } yield cache.put(uuid, result)).unsafeRunAsyncAndForget()

    IO.pure(uuid)
  }

  def findInCache(uuid: UUID): IO[Either[String, ResultResponse]] = {
    IO {
      Option(cache.getIfPresent(uuid)) match {
        case Some(value) => value
        case None        => Left(s"$uuid was not found in cache")
      }
    }
  }

  private def findTitle(response: Response[IO], url: String): IO[Either[String, String]] = {
    response.body
      .through(text.utf8Decode)
      .flatMap(s => Stream.chunk(Chunk.chars(s.toCharArray)))
      .take(urlConnector.settings.webPageLoadLimit)
      .compile
      .to(List)
      .map { chars =>
        val pattern = ".*<title>(.+)</title>.*".r
        chars.mkString match {
          case pattern(title) => Right(title)
          case _              => Left(s"Title for '$url' was not found")
        }
      }
  }
}
