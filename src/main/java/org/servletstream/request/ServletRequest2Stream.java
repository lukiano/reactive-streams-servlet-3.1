package org.servletstream.request;

import org.reactivestreams.api.Producer;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

public final class ServletRequest2Stream {

    private final Producer<Byte> producer;

    public ServletRequest2Stream(final ServletRequest sr, final Executor ex) throws IOException {
        final AtomicReference<RequestSubscription> currentSubscription = new AtomicReference<>();
        final ServletInputStream input = sr.getInputStream();
        final ReadListener readListener = new RequestReadListener(currentSubscription);
        input.setReadListener(readListener);
        producer = new RequestProducer(currentSubscription, input, ex);
    }

    public Producer<Byte> getProducer() {
        return producer;
    }

}
