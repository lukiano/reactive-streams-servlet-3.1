import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import akka.stream.{MaterializerSettings, FlowMaterializer}
import javax.servlet.annotation.WebServlet
import javax.servlet.AsyncContext
import javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpServlet}
import org.reactivestreams.api.Producer

@WebServlet(name = "ReactiveStreamsServlet", urlPatterns = Array("/*"), loadOnStartup = 1, asyncSupported = true)
class Main extends HttpServlet {

  implicit var system: ActorSystem = _

  override def init() {
    system = ActorSystem("Sys")
  }

  private def generator: Iterator[Byte] = (1 to 10000).map(i => i.toByte).iterator

  override protected def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    val materializer = FlowMaterializer(MaterializerSettings())
    val producer: Producer[Byte] =
      Flow(generator).toProducer(materializer)

    Stream2ServletResponse(resp, req.startAsync, producer)
  }

  override protected def doPost(req: HttpServletRequest, resp: HttpServletResponse) {

    val async: AsyncContext = req.startAsync
    ServletRequest2Stream(req, _ => ())
  }

  override def destroy() {
    system.shutdown()
  }

}

