package org.servletstream.request;

import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

final class RequestSubscription implements Subscription {

    private final Subscriber<Byte> subscriber;
    private final ServletInputStream input;
    private final Runnable cancellation;

    public RequestSubscription(final Subscriber<Byte> subscriber,
                               final ServletInputStream input,
                               final Runnable cancellation,
                               final Executor executor) {
        this.subscriber = new AsyncSubscriber<>(executor, subscriber);
        this.input = input;
        this.cancellation = cancellation;
        subscriber.onSubscribe(this);
    }

    public void readAndSend(final int elements) {
        final byte[] buffer = new byte[elements];
        final int readElements;
        try {
            readElements = input.read(buffer);
        } catch (IOException e) {
            cancel();
            signalError(e);
            return;
        }
        if (readElements > 0) {
            for (int i = 0; i < readElements; i++) {
                subscriber.onNext(buffer[i]);
            }
        } else {
            cancel();
            complete();
        }
    }

    public void complete() {
        subscriber.onComplete();
    }

    public void signalError(final Throwable t) {
        subscriber.onError(t);
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

