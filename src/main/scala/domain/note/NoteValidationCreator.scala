package domain.note

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import domain.{NoteNotFoundError, NoteWithSuchTitleExistsError}

class NoteValidationCreator[F[_] : Applicative](noteStorage: NoteStorageInterface[F]) extends NoteValidationInterface[F] {
  override def doesNotExist(note: Note): EitherT[F, NoteWithSuchTitleExistsError, Unit] =
    noteStorage.findByTitle(note.title).map(NoteWithSuchTitleExistsError).toLeft(())

  override def exists(id: Option[Long]): EitherT[F, NoteNotFoundError.type, Unit] =
    id match {
      case Some(id) => noteStorage
        .get(id)
        .toRight(NoteNotFoundError)
        .void
      case None => EitherT.left[Unit](NoteNotFoundError.pure[F])
    }
}

object NoteValidationCreator {
  def apply[F[_] : Applicative](noteStorage: NoteStorageInterface[F]): NoteValidationInterface[F] =
    new NoteValidationCreator[F](noteStorage)
}