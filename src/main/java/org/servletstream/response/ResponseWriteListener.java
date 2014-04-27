package org.servletstream.response;

import javax.servlet.WriteListener;

final class ResponseWriteListener implements WriteListener {

    private final ResponseSubscriber subscriber;

    public ResponseWriteListener(final ResponseSubscriber rs) {
        subscriber = rs;
    }

    @Override
    public void onWritePossible() {
        subscriber.writeNextElement();
    }

    @Override
    public void onError(final Throwable t) {
        subscriber.onError(t);
    }

}
