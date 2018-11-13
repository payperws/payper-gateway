package ws.payper.gateway;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import ws.payper.gateway.config.Api;
import ws.payper.gateway.config.Route;
import ws.payper.gateway.config.RoutePriceConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class PayperGatewayApplication {

    private final RoutePriceConfiguration config;

    @Autowired
    public PayperGatewayApplication(RoutePriceConfiguration config) {
        this.config = config;
    }

    @Autowired
    public PaymentVerifier paymentVerifier;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        config.getApis().forEach(api -> build(api, builder));
        return builder.build();
    }

    private void build(Api api, RouteLocatorBuilder.Builder builder) {
        String baseUrlPath = getPath(api.getBaseUrl()) + "/**";
        builder.route(api.getName(), r -> r.path(baseUrlPath).and().order(1).uri(api.getBaseUrl()));

        api.getRoutes().forEach(route -> build(api, route, builder));
    }

    private void build(Api api, Route route, RouteLocatorBuilder.Builder builder) {
        String routeId = api.getName() + "-" + route.getRoute();

        builder.route(routeId,
                r -> paymentRequiredSpec(r, api, route)
                        .filters(f -> f.redirect(302, redirectUrl(api, route)))
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

    private BooleanSpec paymentRequiredSpec(PredicateSpec spec, Api api, Route route) {
        return spec.predicate(swe -> paymentVerifier.isPaymentRequired(swe, api, route));
    }

    private String getPath(String url) {
        try {
            return new URI(url).getPath();
        } catch (URISyntaxException e) {
            throw new RouteConfigurationException("Could not extract path from URL: " + url, e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PayperGatewayApplication.class, args);
    }
}
