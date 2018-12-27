package ws.payper.gateway.hedera;

public class NetworkCommunicationException extends RuntimeException {

    public NetworkCommunicationException(Throwable e) {
        super(e);
    }

    public NetworkCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
