import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import dataAccess.NoteInMemoryStorage
import cats.implicits._
import domain.note.{NoteService, NoteValidationCreator}
import endpoint.NoteEndpoints
import org.http4s.server.middleware.Logger
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Router
import cats.effect._
import cats.implicits._
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.implicits._
import cats.effect.{ConcurrentEffect, ContextShift,  Timer}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object Server {

  def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    val noteStorage = NoteInMemoryStorage[F]()
    val noteValidation = NoteValidationCreator[F](noteStorage)
    val notesService = NoteService[F](noteStorage, noteValidation)
    val routes = Router("/notes"-> new NoteEndpoints[F].endpoints(notesService)) //.ep(notesService).orNotFound

    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(routes.orNotFound)
      .serve
    } yield exitCode
  }.drain
}