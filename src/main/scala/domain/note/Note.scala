package domain.note

case class Note (title:String, text:String, id:Option[Long] = None)
