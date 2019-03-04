package ws.payper.gateway;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_HEADER;
import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_PARAM;

public class HostOrQueryRoutePredicateFactory extends AbstractRoutePredicateFactory<HostOrQueryRoutePredicateFactory.Config> {

    public HostOrQueryRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return swe -> {
            ServerHttpRequest request = swe.getRequest();
            String receiptHeader = request.getHeaders().getFirst(RECEIPT_HEADER);
            String receiptParam = request.getQueryParams().getFirst(RECEIPT_PARAM);
            return StringUtils.isNotBlank(receiptHeader) || StringUtils.isNotBlank(receiptParam);
        };
    }

    public static class Config {
    }
}
