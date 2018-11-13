package ws.payper.gateway;

public class RouteConfigurationException extends RuntimeException {

    public RouteConfigurationException(Throwable cause) {
        super(cause);
    }

    public RouteConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
