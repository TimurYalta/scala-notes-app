package domain

import domain.note.Note

trait ValidationError extends Product with Serializable

case object NoteNotFoundError extends ValidationError
case class NoteWithSuchTitleExistsError(note: Note) extends ValidationError

