package domain.note

import cats.{Functor, Monad}
import cats.data.EitherT
import domain.{NoteNotFoundError, NoteWithSuchTitleExistsError}

class NoteService[F[_]](noteStorage: NoteStorageInterface[F], noteValidation: NoteValidationInterface[F]) {
  def createNote(note: Note)(implicit M: Monad[F]): EitherT[F, NoteWithSuchTitleExistsError, Note] =
    for {
      _ <- noteValidation.doesNotExist(note)
      saved <- EitherT.liftF(noteStorage.create(note))
    } yield saved

  def getNoteByTitle(title: String)(implicit F: Functor[F]): EitherT[F, NoteNotFoundError.type, Note] =
    noteStorage.findByTitle(title).toRight(NoteNotFoundError)

  def getNotes()(implicit F: Functor[F]): F[List[Note]] = noteStorage.list()
}

object NoteService {
  def apply[F[_]](noteStorage: NoteStorageInterface[F],noteValidation: NoteValidationInterface[F]): NoteService[F] =
    new NoteService[F](noteStorage, noteValidation)
}

