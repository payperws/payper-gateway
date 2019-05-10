package ws.payper.gateway.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RedirectToGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URL;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.setResponseStatus;

public class CustomRedirectToGatewayFilterFactory extends RedirectToGatewayFilterFactory {

    private final Logger log = LoggerFactory.getLogger("PaymentProxy");

    @Override
    public GatewayFilter apply(HttpStatus httpStatus, URL url) {
        return (exchange, chain) ->
                chain.filter(exchange).then(Mono.defer(() -> {
                    if (!exchange.getResponse().isCommitted()) {
                        final ServerHttpResponse response = exchange.getResponse();
                        if (isCheckOnly(exchange)) {
                            log.info("Not redirecting (check only). Status [{}]: Path [{}]",
                                    HttpStatus.PAYMENT_REQUIRED.value(), url.getPath());
                            setResponseStatus(exchange, HttpStatus.PAYMENT_REQUIRED);
                        } else {
                            log.info("Redirecting. Status [{}]: Path [{}]",
                                    httpStatus.value(), url.getPath());
                            setResponseStatus(exchange, httpStatus);
                            response.getHeaders().set(HttpHeaders.LOCATION, url.toString());
                        }
                        return response.setComplete();
                    }
                    return Mono.empty();
                }));
    }

    private boolean isCheckOnly(ServerWebExchange exchange) {
        return Boolean.parseBoolean(exchange.getRequest().getHeaders().getFirst("X-Payment-Quick-Check"));
    }
}
