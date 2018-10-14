package com.example.demogateway;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;

@RestController
@SpringBootApplication
public class PayperGatewayApplication {

@Resource
	private PaymentConfig config;

	@RequestMapping("/payment-required")
	@ResponseBody
	public ResponseEntity paymentRequired() {
		return new ResponseEntity("You need to pay to access the resource", HttpStatus.PAYMENT_REQUIRED);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, RedirectToPaymentDetailsGatewayFilterFactory filterFactory) {
		return builder.routes()
				.route("r1", r -> r.predicate(this::paymentReceiptMissing)
						.filters(f -> f.redirect(302, "http://localhost:8080/payment-required"))
						.uri("http://example.com"))

				.route("r2", r -> r.predicate(this::paymentReceiptValid)
						.filters(f -> f.removeRequestHeader(RECEIPT_HEADER))
						.uri("http://example.com"))

				.build();
	}

	private boolean paymentReceiptValid(ServerWebExchange serverWebExchange) {
		return validReceipt(serverWebExchange);
	}

	private boolean paymentReceiptMissing(ServerWebExchange serverWebExchange) {
		return !validReceipt(serverWebExchange);
	}

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 2);
	}

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {
		return http.httpBasic().and()
				.csrf().disable()
				.authorizeExchange()
				.pathMatchers("/anything/**").authenticated()
				.anyExchange().permitAll()
				.and()
				.build();
	}

	@Bean
	public MapReactiveUserDetailsService reactiveUserDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();
		return new MapReactiveUserDetailsService(user);
	}


	public static final String RECEIPT_HEADER = "X-Payment-Receipt";

	private boolean validReceipt(ServerWebExchange exchange) {
		HttpHeaders headers = exchange.getRequest().getHeaders();
		String receipt = headers.getFirst(RECEIPT_HEADER);
		return !StringUtils.isBlank(receipt);
	}

	public static void main(String[] args) {
		SpringApplication.run(PayperGatewayApplication.class, args);
	}
}
