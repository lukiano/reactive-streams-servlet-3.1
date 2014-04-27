package org.servletstream.request;

import javax.servlet.ReadListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

final class RequestReadListener implements ReadListener {

    public final AtomicReference<RequestSubscription> currentSubscription;

    public RequestReadListener(final AtomicReference<RequestSubscription> current) {
        currentSubscription = current;
    }

    @Override
    public void onDataAvailable() throws IOException {
        RequestSubscription current = currentSubscription.get();
        if (current != null) {
            current.readAndSend(1);
        }
    }

    @Override
    public void onAllDataRead() throws IOException {
        RequestSubscription current = currentSubscription.get();
        if (current != null) {
            current.complete();
        }

    }

    @Override
    public void onError(final Throwable t) {
        RequestSubscription current = currentSubscription.get();
        if (current != null) {
            current.signalError(t);
        }
    }

}
