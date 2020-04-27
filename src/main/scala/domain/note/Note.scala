package domain.note

case class Note (title:String, text:String, id:Option[Long] = None)
case class NoteUpdateRequest(id: Long, note: Note)
case class NotesList(list: List[Note])