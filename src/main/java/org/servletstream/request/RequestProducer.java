package org.servletstream.request;

import org.reactivestreams.api.Consumer;
import org.reactivestreams.api.Producer;
import org.reactivestreams.spi.Publisher;
import org.reactivestreams.spi.Subscriber;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

final class RequestProducer implements Producer<Byte> {

    private final Publisher<Byte> publisher;

    public RequestProducer(final AtomicReference<RequestSubscription> currentSubscription,
                           final ServletInputStream input,
                           final Executor executor) {
        final ReadListener readListener = new RequestReadListener(currentSubscription);
        publisher = new Publisher<Byte>() {

            @Override
            public void subscribe(final Subscriber<Byte> subscriber) {
                Runnable cancellation = new Runnable() {
                    public void run() {
                        currentSubscription.set(null);
                    }
                };
                currentSubscription.set(new RequestSubscription(subscriber, input, cancellation, executor));
                try {
                    readListener.onDataAvailable();
                } catch (IOException ignored) {}
            }

        };
    }

    @Override
    public Publisher<Byte> getPublisher() {
        return publisher;
    }

    @Override
    public void produceTo(final Consumer<Byte> consumer) {
        getPublisher().subscribe(consumer.getSubscriber());
    }

}
