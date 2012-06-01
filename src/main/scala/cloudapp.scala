package dispatch.cloudapp

import scala.util.parsing.json.JSON

object CloudApp {
  import DispatchContrib.ContribVerbs
  import com.ning.http.client.RequestBuilder
  implicit def rc[T](r:RequestBuilder) = new ContribVerbs(r)
}

trait Client {
  import com.ning.http.client.RequestBuilder
  def api: RequestBuilder
}

case class CloudApp(email: String, password: String)
  extends Client
  with Account
  with Items
  with Uploads {
  import dispatch._

  def api = new DispatchContrib.ContribVerbs(
    :/("my.cl.ly").as(email, password)) <:< Map(
      "Accept" -> "application/json",
      "User-Agent" -> "dispatch-cloudapp/0.1.0"
    )
}

trait Account { self: Client =>
  import dispatch._
  def account = api / "account"
  def stats = account / "stats"             
}

trait Items { self: Client =>
  import dispatch._

  def items(
    page: Int = 1,
    perPage: Int = 5,
    `type`: Option[String]= None,
    deleted: Boolean = false,
    source: Option[String] = None) =
      api / "items" <<? Map(
        "page" -> page.toString,
        "per_page" -> perPage.toString,
        "deleted" -> deleted.toString
      ) ++ `type`.map("type" -> _) ++
        source.map("source" -> _)
}

trait Uploads { self: Client =>
  import java.io.File
  import com.ning.http.multipart.{
    StringPart, FilePart
  }
  import dispatch._
  
  private val asJsonMap = As.string andThen {
    JSON.parseFull(_).map {
      _.asInstanceOf[Map[String, Any]]
    }
  }

  def up(file: File) =
    for {
      nup <- Http(api / "items" / "new" > asJsonMap)()
    } yield {
      val remaining = nup("uploads_remaining").toString.toDouble.toInt // cheap/dirty
      if(remaining > 0)
        Right((url(nup("url").toString) /: nup("params").asInstanceOf[Map[String, String]]) {
          case (b, (k, v)) =>
            b.addBodyPart(new StringPart(k, v))
        }.addBodyPart(
          new FilePart("file", file)
        ))
      else Left("over limit")
    }
}
