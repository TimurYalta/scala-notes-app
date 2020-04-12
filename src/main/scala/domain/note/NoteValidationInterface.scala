package domain.note

import cats.data.EitherT
import domain.NoteWithSuchTitleExistsError
import domain.NoteNotFoundError

trait NoteValidationInterface[F[_]] {

  def doesNotExist(note:Note): EitherT[F, NoteWithSuchTitleExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[F, NoteNotFoundError.type, Unit]
}
