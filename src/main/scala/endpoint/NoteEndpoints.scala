package endpoint

import cats.effect.Sync
import domain.note.{Note, NoteService}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import cats.instances.map
import domain.NoteWithSuchTitleExistsError
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.circe._

class NoteEndpoints[F[_] : Sync] extends Http4sDsl[F] {


  implicit val noteDecoder: EntityDecoder[F, Note] = jsonOf


  private def addNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "create" =>
      val action = for {
        note <- req.as[Note]
        result <- noteService.createNote(note).value
      } yield result

      action.flatMap {
        case Right(savedNote: Note) => Ok(savedNote.asJson)
        case Left(NoteWithSuchTitleExistsError(_)) =>
          Conflict(s"Note with such title already exists")
      }
  }

  case class NotesList(list: List[Note])

//  implicit val noteListDecoder: Encoder[NotesList] = jsonOf

  private def getNotes(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root / "all" =>
      val action = for {
        result <- noteService.getNotes()
        packed = NotesList(result)
        jsoned = packed.list.map(_.asJson)
        listJsoned = jsoned.asJson
      } yield listJsoned

//      val resp =
      Ok(action)
  }




  def endpoints(noteService: NoteService[F]): HttpRoutes[F] = addNote(noteService) <+> getNotes(noteService)
}

object NoteEndpoints {
  def endpoints[F[_] : Sync](noteService: NoteService[F]): HttpRoutes[F] = new NoteEndpoints[F].endpoints(noteService)
}