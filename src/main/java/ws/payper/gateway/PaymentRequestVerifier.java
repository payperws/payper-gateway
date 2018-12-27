package ws.payper.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.HeaderRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.config.Api;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.config.Route;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class PaymentRequestVerifier {

    private static final String RECEIPT_HEADER = "X-Payment-Receipt";

    @Autowired
    private PathRoutePredicateFactory pathRoutePredicateFactory;

    @Autowired
    private HeaderRoutePredicateFactory headerRoutePredicateFactory;

    @SuppressWarnings("unused")
    private List<PaymentNetwork> paymentNetworkList;

    private Map<PaymentOptionType, PaymentNetwork> paymentNetworks;

    @Autowired
    public void setPaymentNetworkList(List<PaymentNetwork> paymentNetworkList) {
        this.paymentNetworks = paymentNetworkList.stream().collect(Collectors.toMap(PaymentNetwork::getPaymentOptionType, Function.identity()));
    }

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

    private PaymentNetwork getPaymentNetwork(Api api) {
        PaymentOptionType type = api.getPayment().build().getType();
        PaymentNetwork network = paymentNetworks.get(type);
        if (network == null) {
            throw new IllegalStateException("Could not get a network to support the payment option type: " + type);
        }
        return network;
    }

    private boolean isPaymentProofMissing(ServerWebExchange swe, Api api, Route route) {
        String pattern = getPaymentNetwork(api).getPaymentProofPattern();
        Predicate<ServerWebExchange> headerPredicate = headerRoutePredicateFactory.apply(c -> c.setHeader(RECEIPT_HEADER).setRegexp(pattern));
        return !headerPredicate.test(swe);
    }

    private boolean receiptNetworkVerificationFailed(ServerWebExchange swe, Api api, Route route) {
        String paymentProof = Objects.requireNonNull(swe.getRequest().getHeaders().get(RECEIPT_HEADER)).stream().findFirst().orElse("");
        PaymentEndpoint paymentEndpoint = api.getPayment().build();
        String amount = route.getPrice();
        boolean transactionVerified = getPaymentNetwork(api).verifyTransaction(paymentProof, paymentEndpoint, amount);
        return !transactionVerified;
    }
}
