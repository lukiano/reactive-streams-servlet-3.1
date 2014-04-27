package org.servletstream.request;

import org.reactivestreams.spi.Subscriber;
import org.reactivestreams.spi.Subscription;

import java.util.concurrent.Executor;

final class AsyncSubscriber<T> implements Subscriber<T> {

    private final Executor executor;
    private final Subscriber<T> delegate;

    public AsyncSubscriber(Executor executor, Subscriber<T> delegate) {
        this.executor = executor;
        this.delegate = delegate;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        delegate.onSubscribe(subscription);
    }

    @Override
    public void onNext(final T element) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                delegate.onNext(element);
            }
        });

    }

    @Override
    public void onComplete() {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                delegate.onComplete();
            }
        });

    }

    @Override
    public void onError(final Throwable cause) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                delegate.onError(cause);
            }
        });

    }
}
