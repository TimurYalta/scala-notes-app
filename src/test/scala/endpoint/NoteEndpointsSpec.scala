package endpoint


import cats.effect._
import dataAccess.NoteInMemoryStorage
import domain.note.{Note, NoteService, NoteValidationCreator}
import io.circe._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.server.Router
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.matchers.should.Matchers

class NoteEndpointsSpec
  extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with NoteArbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {


  implicit val NoteEncoder: Encoder[Note] = deriveEncoder
  implicit val NoteEnc: EntityEncoder[IO, Note] = jsonEncoderOf
  implicit val NoteDecoder: Decoder[Note] = deriveDecoder
  implicit val NoteDec: EntityDecoder[IO, Note] = jsonOf

  def getTestResources(): (HttpApp[IO]) = {
    val noteStorage = NoteInMemoryStorage[IO]()
    val noteValidation = NoteValidationCreator[IO](noteStorage)
    val notesService = NoteService[IO](noteStorage, noteValidation)
    val routes = Router("/notes"-> new NoteEndpoints[IO].endpoints(notesService))
    routes.orNotFound
  }

  test("place and get Note") {
    val NoteRoutes = getTestResources()

    forAll { (note: Note) =>
      (for {
        createRq <- POST(note, uri"/notes/create")
        createResp <- NoteRoutes.run(createRq)
        noteResp <- createResp.as[Note]
        uri = Uri.unsafeFromString(s"/notes/title/${noteResp.title}")
        getNoteRq <- GET(uri)
        getNoteResp <- NoteRoutes.run(getNoteRq)
        noteResp2 <- getNoteResp.as[Note]
      } yield {
        createResp.status shouldEqual Ok
        noteResp.title shouldEqual note.title
        getNoteResp.status shouldEqual Ok
        noteResp2.id shouldBe defined
      }).unsafeRunSync
    }
  }

  test("create and delete Note") {
    val NoteRoutes = getTestResources()

    forAll { (note: Note) =>
      (for {
        createRq <- POST(note, uri"/notes/create")
        createResp <- NoteRoutes.run(createRq)
        noteResp <- createResp.as[Note]
        deleteUri = Uri.unsafeFromString(s"/notes/delete/title/${noteResp.title}")
        deleteNoteRq <- GET(deleteUri)
        deleteNoteResp <- NoteRoutes.run(deleteNoteRq)
        noteResp2 <- deleteNoteResp.as[Note]
        getUri = Uri.unsafeFromString(s"/notes/title/${noteResp.title}")
        getNoteRq <- GET(getUri)
        getNoteResp <- NoteRoutes.run(getNoteRq)
      } yield {
        createResp.status shouldEqual Ok
        noteResp.title shouldEqual note.title
        noteResp2.title shouldEqual note.title
        getNoteResp.status shouldEqual NotFound
      }).unsafeRunSync
    }
  }


  test("update note") {
    val NoteRoutes = getTestResources()

    forAll { (note: Note) =>
      (for {
        createRq <- POST(note, uri"/notes/create")
        createResp <- NoteRoutes.run(createRq)
        createRespNote <- createResp.as[Note]
        updateUri = Uri.unsafeFromString(s"/notes/update/${createRespNote.id.get}")
        updatedNote = Note(s"updated${createRespNote.title}", createRespNote.text)
        updateRq <- POST(updatedNote, updateUri)
        updateResp <- NoteRoutes.run(updateRq)
        getUri = Uri.unsafeFromString(s"/notes/title/updated${note.title}")
        getRq <- GET(getUri)
        getResp <- NoteRoutes.run(getRq)
        getRespNote <- getResp.as[Note]
      } yield {
        createResp.status shouldEqual Ok
        createRespNote.id shouldBe defined
        updateResp.status shouldEqual Ok
        getResp.status shouldEqual Ok
        getRespNote.title shouldEqual s"updated${note.title}"
      }).unsafeRunSync
    }
  }

}
