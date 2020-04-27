package dataAccess

import cats.Applicative
import cats.data.OptionT
import cats.effect.Sync
import domain.note.{Note, NoteStorageInterface}

import scala.collection.mutable
import cats.implicits._

import scala.util.Random


class NoteInMemoryStorage[F[_] : Sync] extends NoteStorageInterface[F] {
  private val noteMap = new mutable.HashMap[Long, Note]()
  private val rand = new Random()

  override def create(note: Note): F[Note] = Sync[F].delay {
    val id = Math.abs(rand.nextLong)
    val noteWithId = note.copy(id = id.some)
    noteMap += (id -> noteWithId)
    noteWithId
  }

  override def get(id: Long): OptionT[F, Note] = OptionT.fromOption(noteMap.get(id))

  override def findByTitle(title: String): OptionT[F, Note] = OptionT.fromOption(noteMap.values.find(u => u.title == title))


  override def list(): F[List[Note]] = noteMap.values.toList.pure[F]

  override def deleteById(id: Long): F[OptionT[F, Note]] = Sync[F].delay {
    val note = noteMap.remove(id)
    OptionT.fromOption(note)
  }

  override def deleteByTitle(title: String): F[OptionT[F, Note]] = Sync[F].delay {
    val note = noteMap.values.find(u => u.title == title)
    note match {
      case Some(found) => OptionT.fromOption {
        found.id match {
          case Some(id) => noteMap.remove(id)
          case None => None
        }
      }
      case None => OptionT.none
    }
  }

  override def edit(id: Long, toEdit: Note): F[OptionT[F, Note]] = Sync[F].delay{
    val note = noteMap.values.find(u => u.id.contains(id))
    note match {
      case Some(found) => OptionT.fromOption {
        found.id match {
          case Some(id) => {
            noteMap.remove(id)
            noteMap += (id -> toEdit.copy(id = id.some))
            noteMap.get(id)
          }
          case None => None
        }
      }
      case None => OptionT.none
    }
  }
}

object NoteInMemoryStorage {
  def apply[F[_] : Sync](): NoteInMemoryStorage[F] = new NoteInMemoryStorage[F]()
}
