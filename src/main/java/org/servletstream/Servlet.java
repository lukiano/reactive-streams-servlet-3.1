package org.servletstream;

import org.servletstream.request.ServletRequest2Stream;
import org.servletstream.response.Stream2ServletResponse;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "ReactiveStreamsServlet", urlPatterns = "/*", loadOnStartup = 1, asyncSupported = true)
public final class Servlet extends HttpServlet {

    private ExecutorService executorService;

    @Override
    public void init() {
        executorService = new ForkJoinPool(4);
    }

    @Override
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(4, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final AsyncContext async = req.startAsync();
        final ServletRequest2Stream servletRequest2Stream = new ServletRequest2Stream(req, executorService);
        new Stream2ServletResponse(resp, async, servletRequest2Stream.getProducer());
    }

}
