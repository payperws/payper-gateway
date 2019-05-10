package ws.payper.gateway.web;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }

    public LinkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
