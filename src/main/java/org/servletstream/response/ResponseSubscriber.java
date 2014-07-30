package org.servletstream.response;

import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

final class ResponseSubscriber implements Subscriber<Byte> {

    private final ServletOutputStream output;
    private final AsyncContext async;
    private final AtomicReference<Subscription> currentSubscription = new AtomicReference<>();
    private final AtomicReference<Byte> nextElement = new AtomicReference<>();

    public ResponseSubscriber(final ServletOutputStream output,
                              final AsyncContext async) {
        this.output = output;
        this.async = async;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        currentSubscription.set(subscription);
    }

    void write(final Byte element) {
        try {
            output.write(element.intValue());
            currentSubscription.get().requestMore(1);
        } catch (IOException e) {
            onError(e);
        }
    }

    public void writeNextElement() {
        Byte next = nextElement.getAndSet(null);
        if (next != null) {
            write(next);
        }
    }

    @Override
    public void onNext(final Byte element) {
        if (output.isReady()) {
            write(element);
        } else {
            nextElement.set(element);
        }
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

}
