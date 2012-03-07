package dispatch.cloudapp


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
  extends Client with Items with Account {
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

