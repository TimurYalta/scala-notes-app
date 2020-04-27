package endpoint
import domain.note.Note
import org.scalacheck._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen._

trait NoteArbitraries {

  implicit val pet = Arbitrary[Note] {
    for {
      title <- Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)
      text <- arbitrary[String]
    } yield Note(title, text)
  }
}

object NoteArbitraries extends NoteArbitraries
