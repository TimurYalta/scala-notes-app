package domain.note

import cats.{Functor, Monad}
import cats.data.{EitherT, OptionT}
import domain.{NoteNotFoundError, NoteWithSuchTitleExistsError}
import tofu.syntax.monadic._
import cats.instances._

class NoteService[F[_]](noteStorage: NoteStorageInterface[F], noteValidation: NoteValidationInterface[F]) {
  def createNote(note: Note)(implicit M: Monad[F]): EitherT[F, NoteWithSuchTitleExistsError, Note] =
    for {
      _ <- noteValidation.doesNotExist(note)
      saved <- EitherT.liftF(noteStorage.create(note))
    } yield saved

  def editNote(id: Long, note: Note)(implicit M: Monad[F]): EitherT[F, NoteNotFoundError.type , Note] =
    OptionT.liftF(noteStorage.edit(id,note)).flatten.toRight(NoteNotFoundError)

  def getNoteByTitle(title: String)(implicit F: Functor[F]): EitherT[F, NoteNotFoundError.type, Note] =
    noteStorage.findByTitle(title).toRight(NoteNotFoundError)

  def getNotes()(implicit F: Functor[F]): F[List[Note]] = noteStorage.list()

  def deleteNoteById(id: Long)(implicit M: Monad[F]): EitherT[F, NoteNotFoundError.type , Note] =
    OptionT.liftF(noteStorage.deleteById(id)).flatten.toRight(NoteNotFoundError)

  def deleteNoteByTitle(title: String)(implicit M: Monad[F]): EitherT[F, NoteNotFoundError.type , Note] =
    OptionT.liftF(noteStorage.deleteByTitle(title)).flatten.toRight(NoteNotFoundError)

}

object NoteService {
  def apply[F[_]](noteStorage: NoteStorageInterface[F],noteValidation: NoteValidationInterface[F]): NoteService[F] =
    new NoteService[F](noteStorage, noteValidation)
}
