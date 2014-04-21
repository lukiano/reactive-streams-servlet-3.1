import java.util.concurrent.atomic.AtomicReference
import javax.servlet.{ServletInputStream, ReadListener, ServletRequest}
import org.reactivestreams.api.{Consumer, Producer}
import org.reactivestreams.spi.{Subscription, Subscriber, Publisher}

object ServletRequest2Stream {

  def apply(sr: ServletRequest, callback: Producer[Byte] => Unit) = new ServletRequest2Stream(sr, callback)

  class MySubscription(val subscriber: Subscriber[Byte],
                       input: ServletInputStream,
                       cancellation: () => Unit) extends Subscription {

    subscriber.onSubscribe(this)

    def readAndSend(elements: Int): Unit = {
      val buffer = Array.ofDim[Byte](elements)
      val readElements = input.read(buffer)
      if (readElements > 0) {
        buffer.take(readElements).foreach(subscriber.onNext)
      } else {
        subscriber.onComplete()
        cancel()
      }
    }

    override def requestMore(elements: Int): Unit = {
      if (input.isReady) {
        readAndSend(elements)
      }
    }

    override def cancel(): Unit = {
      cancellation()
    }

  }

}

class ServletRequest2Stream(sr: ServletRequest, callback: Producer[Byte] => Unit) {

  import ServletRequest2Stream.MySubscription

  private val input = sr.getInputStream
  private val currentSubscription: AtomicReference[Option[MySubscription]] = new AtomicReference[Option[MySubscription]]()
  currentSubscription.set(None)
  private val readListener = new ReadListener {

    override def onDataAvailable(): Unit = {
      currentSubscription.get match {
        case Some(subscription) =>
          subscription.readAndSend(1)
        case None =>
      }
    }

    override def onAllDataRead(): Unit = {
      currentSubscription.get match {
        case Some(subscription) =>
          subscription.subscriber.onComplete()
          currentSubscription.set(None)
        case None =>
      }

    }

    override def onError(t: Throwable): Unit = {
      currentSubscription.get match {
        case Some(subscription) =>
          subscription.subscriber.onError(t)
        case None =>
      }

    }

  }

  input.setReadListener(readListener)

  private val producer = new Producer[Byte] {

    override def getPublisher: Publisher[Byte] = {
      new Publisher[Byte] {

        override def subscribe(subscriber: Subscriber[Byte]): Unit = {
          currentSubscription.set(Some(new MySubscription(subscriber, input, () => currentSubscription.set(None) )))
          readListener.onDataAvailable()
        }
      }
    }

    override def produceTo(consumer: Consumer[Byte]): Unit = {
      getPublisher.subscribe(consumer.getSubscriber)
    }

  }

  callback(producer)

}
