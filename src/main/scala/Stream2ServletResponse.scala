import akka.stream.scaladsl.Flow
import akka.stream.{MaterializerSettings, FlowMaterializer}
import java.util.concurrent.Semaphore
import javax.servlet.{ServletResponse, WriteListener, AsyncContext}
import org.reactivestreams.api.Producer
import scala.util.Try

object Stream2ServletResponse {

  def apply(sr: ServletResponse, async: AsyncContext, producer: Producer[Byte]) =
    new Stream2ServletResponse(sr, async, producer)

}

class Stream2ServletResponse(sr: ServletResponse, async: AsyncContext, producer: Producer[Byte]) {

  private val materializer = FlowMaterializer(MaterializerSettings())

  private val semaphore = new Semaphore(0)
  private val writeListener = new WriteListener {

    override def onWritePossible(): Unit = {
      semaphore.release()
    }

    override def onError(t: Throwable): Unit = {
      producer.getPublisher.
    }

  }

  private val output = sr.getOutputStream
  output.setWriteListener(writeListener)

  Flow(producer).
    foreach { byte =>
    if (!output.isReady) {
      semaphore.acquireUninterruptibly()
    }
    output.write(byte)
  }.
    onComplete(materializer) { _ =>
    Try(async.complete())
  }

}
