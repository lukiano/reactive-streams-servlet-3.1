import org.reactivestreams.api.Consumer;
import org.reactivestreams.api.Producer;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class ServletRequest2Stream {

    private static final class MySubscription implements Subscription {

        private final Subscriber<Byte> subscriber;
        private final ServletInputStream input;
        private final Runnable cancellation;

        public MySubscription(final Subscriber<Byte> subscriber,
                              final ServletInputStream input,
                              final Runnable cancellation) {
            this.subscriber = subscriber;
            this.input = input;
            this.cancellation = cancellation;
            subscriber.onSubscribe(this);
        }

        public void readAndSend(final int elements) {
            byte[] buffer = new byte[elements];
            int readElements;
            try {
                readElements = input.read(buffer);
            } catch (IOException e) {
                subscriber.onError(e);
                return;
            }
            if (readElements > 0) {
                for (int i = 0; i < readElements; i++) {
                    subscriber.onNext(buffer[i]);
                }
            } else {
                subscriber.onComplete();
                cancel();
            }
        }

        @Override
        public void requestMore(final int elements) {
            if (input.isReady()) {
                readAndSend(elements);
            }
        }

        @Override
        public void cancel() {
            cancellation.run();
        }

    }

    private final ServletInputStream input;
    private final AtomicReference<MySubscription> currentSubscription = new AtomicReference<>(null);
    private final ReadListener readListener = new ReadListener() {

        @Override
        public void onDataAvailable() throws IOException {
            MySubscription current = currentSubscription.get();
            if (current != null) {
                current.readAndSend(1);
            }
        }

        @Override
        public void onAllDataRead() throws IOException {
            MySubscription current = currentSubscription.get();
            if (current != null) {
                current.subscriber.onComplete();
            }

        }

        @Override
        public void onError(final Throwable t) {
            MySubscription current = currentSubscription.get();
            if (current != null) {
                current.subscriber.onError(t);
            }
        }
    };

    private final Producer<Byte> producer = new Producer<Byte>() {

        @Override
        public Publisher<Byte> getPublisher() {
            return new Publisher<Byte>() {
                public void subscribe(final Subscriber<Byte> subscriber) {
                    Runnable cancellation = new Runnable() {
                        public void run() {
                            currentSubscription.set(null);
                        }
                    };
                    currentSubscription.set(new MySubscription(subscriber, input, cancellation));
                    try {
                        readListener.onDataAvailable();
                    } catch (IOException ignored) {}
                }
            };
        }

        @Override
        public void produceTo(final Consumer<Byte> consumer) {
            getPublisher().subscribe(consumer.getSubscriber());
        }
    };

    public ServletRequest2Stream(final ServletRequest sr) throws IOException {
        input = sr.getInputStream();
        input.setReadListener(readListener);
    }

    public Producer<Byte> getProducer() {
        return producer;
    }

}
