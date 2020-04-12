package dataAccess

import cats.Applicative
import cats.data.OptionT
import domain.note.{Note, NoteStorageInterface}

import scala.collection.mutable
import cats.implicits._

import scala.util.Random


class NoteInMemoryStorage[F[_]: Applicative] extends NoteStorageInterface[F]{
  private val noteMap = new mutable.HashMap[Long, Note]()
  private val rand = new Random()

  override def create(note: Note): F[Note] = {
    val id = Math.abs(rand.nextLong)
    val noteWithId = note.copy(id = id.some)
    noteMap += (id -> noteWithId)
    noteWithId.pure[F]
  }

  override def get(id: Long): OptionT[F, Note] = OptionT.fromOption(noteMap.get(id))
//
//  override def edit(id: Long): OptionT[F, Note] = ???
//
  override def findByTitle(title: String): OptionT[F, Note] = OptionT.fromOption(noteMap.values.find(u => u.title == title))
//
//  override def deleteById(id: Long): OptionT[F, Note] = ???
//
//  override def deleteByTitle(id: Long): OptionT[F, Note] = ???
//
  override def list(): F[List[Note]] = noteMap.values.toList.pure[F]
}
object NoteInMemoryStorage{
  def apply[F[_]:Applicative](): NoteInMemoryStorage[F] = new NoteInMemoryStorage[F]()
}
