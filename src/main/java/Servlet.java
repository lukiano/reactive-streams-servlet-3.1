import javax.servlet.AsyncContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ReactiveStreamsServlet", urlPatterns = "/*", loadOnStartup = 1, asyncSupported = true)
public final class Servlet extends HttpServlet {

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final AsyncContext async = req.startAsync();
        final ServletRequest2Stream servletRequest2Stream = new ServletRequest2Stream(req);
        new Stream2ServletResponse(resp, async, servletRequest2Stream.getProducer());
    }

}
