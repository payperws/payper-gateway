package ws.payper.gateway;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.config.RoutePriceConfiguration;

@SpringBootApplication
public class PayperGatewayApplication {

    public static final String RECEIPT_HEADER = "X-Payment-Receipt";

    private final RoutePriceConfiguration config;

    @Autowired
    public PayperGatewayApplication(RoutePriceConfiguration config) {
        this.config = config;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("r1", r -> r.predicate(this::paymentReceiptMissing)
                        .filters(f -> f.redirect(302, "http://localhost:8080/payment-required"))
                        .uri("http://swapi.co/"))

                .route("r2", r -> r.predicate(this::paymentReceiptValid)
                        .filters(f -> f.removeRequestHeader(RECEIPT_HEADER))
                        .uri("http://swapi.co/"))

                .build();
    }

    private boolean paymentReceiptValid(ServerWebExchange serverWebExchange) {
        return validReceipt(serverWebExchange);
    }

    private boolean paymentReceiptMissing(ServerWebExchange serverWebExchange) {
        return !validReceipt(serverWebExchange);
    }

    private boolean validReceipt(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String receipt = headers.getFirst(RECEIPT_HEADER);
        return !StringUtils.isBlank(receipt);
    }

    public static void main(String[] args) {
        SpringApplication.run(PayperGatewayApplication.class, args);
    }

}
