package domain.note

import cats.data.OptionT

trait NoteStorageInterface[F[_]] {
  def create(note: Note): F[Note]

  def get(id: Long): OptionT[F, Note]
//
//  def edit(id: Long): OptionT[F, Note]
//
  def findByTitle(title: String): OptionT[F, Note]
//
//  def deleteById(id: Long): OptionT[F, Note]
//
//  def deleteByTitle(id: Long): OptionT[F, Note]
//
  def list(): F[List[Note]]
}
