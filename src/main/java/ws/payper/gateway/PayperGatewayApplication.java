package ws.payper.gateway;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.config.Api;
import ws.payper.gateway.config.Route;
import ws.payper.gateway.config.RoutePriceConfiguration;

import java.net.URISyntaxException;

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
        RouteLocatorBuilder.Builder routes = builder.routes();
        config.getApis().forEach(api -> build(api, routes));
        return routes.build();
    }

    private void build(Api api, RouteLocatorBuilder.Builder builder) {
        api.getRoutes().forEach(route -> build(api, route, builder));
    }

    private void build(Api api, Route route, RouteLocatorBuilder.Builder builder) {
        String routeId = api.getName() + "-" + route.getRoute();
        builder
                .route(routeId + "-r1",
                        r -> r.predicate(this::paymentReceiptMissing)
                                .filters(f -> f.redirect(302, redirectUrl(api, route)))
                                .uri(api.getBaseUrl()))
                .route(routeId +"-r2",
                        r -> r.predicate(this::paymentReceiptValid)
                                .filters(f -> f.removeRequestHeader(RECEIPT_HEADER))
                                .uri(api.getBaseUrl()));
    }

    private String redirectUrl(Api api, Route route) {
        String redirectUrl = getPaymentRequiredUrl();
        try {
            return new URIBuilder(redirectUrl)
                    .addParameter("amount", route.getPrice())
                    .addParameter("account", api.getWalletAddress())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RouteConfigurationException(e);
        }
    }

    private String getPaymentRequiredUrl() {
        return "http://localhost:8080/payment-required";
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
