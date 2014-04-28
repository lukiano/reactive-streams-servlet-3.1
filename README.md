reactive-streams-servlet-3.1
============================

Reader side:
Feed an incoming network stream from a Servlet request to a reactive stream. Will only read data as requested from the reactive stream.
It will provide data as long as it is available, else it will wait (actually the consumer will wait) until it becomes available.

Writer side:
Feed an outgoing stream by consuming a reactive stream. It will stop consuming if it cannot write to the stream, and resume once it can (thus applying backpressure).
This uses the Servlet 3.1 listeners.
