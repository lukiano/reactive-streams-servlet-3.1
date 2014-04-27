package org.servletstream.response;

import org.reactivestreams.api.Producer;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import java.io.IOException;

public final class Stream2ServletResponse {

    public Stream2ServletResponse(final ServletResponse servletResponse,
                                  final AsyncContext asyncContext,
                                  final Producer<Byte> producer) throws IOException {

        final ServletOutputStream output = servletResponse.getOutputStream();
        final ResponseSubscriber subscriber = new ResponseSubscriber(output, asyncContext);
        output.setWriteListener(new ResponseWriteListener(subscriber));
        producer.getPublisher().subscribe(subscriber);
    }
}
