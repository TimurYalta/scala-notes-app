package endpoint

import cats.effect.Sync
import domain.note.{Note, NoteService, NoteUpdateRequest, NotesList}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import cats.instances.map
import domain.{NoteNotFoundError, NoteWithSuchTitleExistsError}
import io.circe.Encoder
import io.circe.generic.auto._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.circe._

class NoteEndpoints[F[_] : Sync] extends Http4sDsl[F] {


  implicit val noteDecoder: EntityDecoder[F, Note] = jsonOf
  implicit val noteUpdateReqDecoder: EntityDecoder[F, NoteUpdateRequest] = jsonOf


  private def addNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "create" =>
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

  private def updateNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "update" / LongVar(id) =>
      val action = for {
        note <- req.as[Note]
        result <- noteService.editNote(id, note).value
      } yield result

      action.flatMap {
        case Right(updatedNote: Note) => Ok(updatedNote.asJson)
        case Left(NoteNotFoundError) => Conflict(s"Note with such id not found")
      }
  }


  //
  private def deleteNote(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@GET -> Root / "delete" / LongVar(id) =>
      val result = noteService.deleteNoteById(id).value
      result.flatMap {
        case Right(deletedNote: Note) => Ok(deletedNote.asJson)
        case Left(NoteNotFoundError) => Conflict(s"Note with such id not found")
      }
  }

  private def deleteNoteByTitle(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@GET -> Root / "delete" / "title" / title =>
      val result = noteService.deleteNoteByTitle(title).value
      result.flatMap {
        case Right(deletedNote: Note) => Ok(deletedNote.asJson)
        case Left(NoteNotFoundError) => Conflict(s"Note with such id not found")
      }
  }


  private def getNotes(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@GET -> Root / "all" =>
      val action = for {
        result <- noteService.getNotes()
        packed = NotesList(result)
        jsoned = packed.list.map(_.asJson)
        listJsoned = jsoned.asJson
      } yield listJsoned

      Ok(action)
  }

  private def getNoteByTitle(noteService: NoteService[F]): HttpRoutes[F] = HttpRoutes.of[F] {
    case req@GET -> Root / "title" / title =>
      noteService.getNoteByTitle(title).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(NoteNotFoundError) => NotFound("The note with such title was not found")
      }
  }


  def endpoints(noteService: NoteService[F]): HttpRoutes[F] = {
    addNote(noteService) <+>
      getNotes(noteService) <+>
      deleteNote(noteService) <+>
      deleteNoteByTitle(noteService) <+>
      getNoteByTitle(noteService) <+>
      updateNote(noteService)

  }
}

object NoteEndpoints {
  def endpoints[F[_] : Sync](noteService: NoteService[F]): HttpRoutes[F] = new NoteEndpoints[F].endpoints(noteService)
}