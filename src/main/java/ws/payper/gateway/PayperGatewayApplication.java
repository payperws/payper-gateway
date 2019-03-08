package ws.payper.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import ws.payper.gateway.proxy.CustomRedirectToGatewayFilterFactory;
import ws.payper.gateway.proxy.HeaderOrParamRoutePredicateFactory;
import ws.payper.gateway.proxy.PaymentRequiredRoutePredicateFactory;

@SpringBootApplication
public class PayperGatewayApplication {

    public PayperGatewayApplication() {
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes().build();
    }

    @Bean
    public PaymentRequiredRoutePredicateFactory paymentRequiredRoutePredicateFactory() {
        return new PaymentRequiredRoutePredicateFactory();
    }

    @Bean
    public CustomRedirectToGatewayFilterFactory customRedirectToRoutePredicateFactory() {
        return new CustomRedirectToGatewayFilterFactory();
    }

    @Bean
    public HeaderOrParamRoutePredicateFactory headerOrParamRoutePredicateFactory() {
        return new HeaderOrParamRoutePredicateFactory();
    }

    public static void main(String[] args) {
        SpringApplication.run(PayperGatewayApplication.class, args);
    }
}
