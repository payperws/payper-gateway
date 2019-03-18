package ws.payper.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ws.payper.gateway.proxy.CustomRedirectToGatewayFilterFactory;
import ws.payper.gateway.proxy.HeaderOrParamRoutePredicateFactory;
import ws.payper.gateway.proxy.PaymentRequiredRoutePredicateFactory;
import ws.payper.gateway.service.RouteService;

@EnableMongoRepositories(basePackages = "ws.payper.gateway.repo")
@SpringBootApplication
public class PayperGatewayApplication {

    @Autowired
    private RouteService routeService;

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

    @EventListener(ApplicationReadyEvent.class)
    public void loadRoutesFromRepo() {
        routeService.preload();
    }

    public static void main(String[] args) {
        SpringApplication.run(PayperGatewayApplication.class, args);
    }
}
