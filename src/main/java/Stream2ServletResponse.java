import org.reactivestreams.api.Producer;
import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class Stream2ServletResponse {

    private final Semaphore semaphore = new Semaphore(0);
    private final Subscriber<Byte> subscriber = new Subscriber<Byte>() {

        private final AtomicReference<Subscription> currentSubscription = new AtomicReference<>(null);

        @Override
        public void onSubscribe(final Subscription subscription) {
            currentSubscription.set(subscription);
        }

        public void write(final Byte element) {
            try {
                output.write(element.intValue());
                currentSubscription.get().requestMore(1);
            } catch (IOException e) {
                onError(e);
            }
        }

        @Override
        public void onNext(final Byte element) {
            if (!output.isReady()) {
                semaphore.acquireUninterruptibly();
            }
            write(element);
        }

        @Override
        public void onComplete() {
            async.complete();
        }

        @Override
        public void onError(final Throwable cause) {
            Subscription current = currentSubscription.get();
            if (current != null) {
                current.cancel();
            }
        }
    };

    private final ServletOutputStream output;
    private final AsyncContext async;

    public Stream2ServletResponse(final ServletResponse servletResponse,
                                  final AsyncContext asyncContext,
                                  final Producer<Byte> producer) throws IOException {
        output = servletResponse.getOutputStream();
        async = asyncContext;
        final WriteListener writeListener = new WriteListener() {

            @Override
            public void onWritePossible() {
                semaphore.release();
            }

            @Override
            public void onError(final Throwable t) {
                subscriber.onError(t);
            }
        };
        output.setWriteListener(writeListener);
        producer.getPublisher().subscribe(subscriber);
    }
}
