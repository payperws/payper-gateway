package com.example.demogateway;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RedirectToPaymentDetailsGatewayFilterFactory extends AbstractGatewayFilterFactory<PaymentConfig> {


    public static final String RECEIPT_HEADER = "X-Payment-Receipt";

    public static final String PAYMENT_REQUIRED_URL = "http://localhost:8080/payment-required";

    @Override
    public GatewayFilter apply(PaymentConfig config) {
        return ((exchange, chain) -> addPaymentDetailsAndRedirect(exchange, chain, config));
    }

    private Mono<Void> addPaymentDetailsAndRedirect(ServerWebExchange exchange, GatewayFilterChain chain, PaymentConfig config) {

        // if X-Payment-Receipt header missing or format invalid, forward to /payment-requested with 402 status and X-Required-Payment-Address and X-Required-Payment-Amount
        if (!validReceipt(exchange)) {
        }

        // if X-Payment-Receipt not found (after configured timeout), 402 and required payment headers.

        // Forwards request to resource. Removes X-Payment-Receipt.

        return chain.filter(exchange);
    }

    private boolean validReceipt(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String receipt = headers.getFirst(RECEIPT_HEADER);
        return !StringUtils.isBlank(receipt);
    }
}
