package dispatch.cloudapp

object DispatchContrib {
  import com.ning.http.client.{
    AsyncCompletionHandler, RequestBuilder, Response, Realm
  }
  import dispatch._

  trait FileVerbs extends RequestVerbs {
    import java.io.File
    def <<< (file: File) =
      subject.PUT.setBody(file)
    def << (file: File) =
      subject.POST.setBody(file)
  }

  trait ExtraAuthVerbs extends RequestVerbs {
    import com.ning.http.client.Realm.RealmBuilder
    def digest (user: String, password: String, realm: Option[String] = None) =
      subject.setRealm(new RealmBuilder()
                     .setPrincipal(user)
                     .setPassword(password)
                     //.setUsePreemptiveAuth(false)
                     //.setScheme(Realm.AuthScheme.DIGEST)
                     .build())
   }

  object ProgressVerbs {
    import Progress._
    class WriteProgressHandler[T](f: Response => T)(p: Listener)
    extends FunctionHandler[T](f) {
      override def onContentWriteProgress(amount: Long, current: Long, total: Long) = {
        p(amount, current, total)
        super.onContentWriteProgress(amount, current, total)
      }
    }
  }

  object Progress {
    import ProgressVerbs._
    type Listener = (Long, Long, Long) => Unit
    def apply[T](f: Response => T)(p: Listener) =
      new WriteProgressHandler(f)(p)
  }

  trait DebugParams extends RequestVerbs {
    def debugp(ps: Traversable[(String, String)]) = {
      println("adding query params")
      (subject /: ps) {
        case (s, (key, value)) =>
          s.addQueryParameter(key, value)
      }
    }
  }

  trait HeaderVerbs extends RequestVerbs {
    def headers(hs: Traversable[(String, String)]) =
      (subject /: hs) {
        case (s, (key, value)) =>
          s.addHeader(key, value)
      }
    def <:< (hs: Traversable[(String, String)]) =
      headers(hs)
  }

  class ContribVerbs(sub: RequestBuilder)
    extends DefaultRequestVerbs(sub)
    with HeaderVerbs
    with FileVerbs
    with ExtraAuthVerbs
    with DebugParams
}
