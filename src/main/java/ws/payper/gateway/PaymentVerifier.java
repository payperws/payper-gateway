package ws.payper.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.HeaderRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.config.Api;
import ws.payper.gateway.config.Route;

import java.util.Objects;
import java.util.function.Predicate;

@Component
public class PaymentVerifier {

    private static final String RECEIPT_HEADER = "X-Payment-Receipt";

    private static final String HEADER_PATTERN = "^[0-9]{8,10}\\|[0-9]{1,9}\\|[0-9]{4,10}";

    @Autowired
    private PathRoutePredicateFactory pathRoutePredicateFactory;

    @Autowired
    private HeaderRoutePredicateFactory headerRoutePredicateFactory;

    public boolean isPaymentRequired(ServerWebExchange swe, Api api, Route route) {
        return isRequestMatching(swe, api, route) && (isPaymentProofMissing(swe, api, route) || receiptNetworkVerificationFailed(swe, api, route));
    }

    private boolean isRequestMatching(ServerWebExchange swe, Api api, Route route) {
        return methodMatches(swe, HttpMethod.GET) && pathMatches(swe, api, route);
    }

    private boolean methodMatches(ServerWebExchange swe, HttpMethod method) {
        return Objects.equals(swe.getRequest().getMethod(), method);
    }

    private boolean pathMatches(ServerWebExchange swe, Api api, Route route) {
        Predicate<ServerWebExchange> routePredicate = pathRoutePredicateFactory.apply(c -> c.setPattern(route.getRoute()));
        return routePredicate.test(swe);
    }

    private boolean isPaymentProofMissing(ServerWebExchange swe, Api api, Route route) {
        Predicate<ServerWebExchange> headerPredicate = headerRoutePredicateFactory.apply(c -> c.setHeader(RECEIPT_HEADER).setRegexp(HEADER_PATTERN));
        return !headerPredicate.test(swe);
    }

    private boolean receiptNetworkVerificationFailed(ServerWebExchange swe, Api api, Route route) {
        boolean failed = false;
        return failed;
    }
}
