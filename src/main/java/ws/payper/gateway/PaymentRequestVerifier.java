package ws.payper.gateway;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.HeaderRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.QueryRoutePredicateFactory;
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

    private static final String RECEIPT_PARAM = "x_payment_receipt";

    @Autowired
    private PathRoutePredicateFactory pathRoutePredicateFactory;

    @Autowired
    private HeaderRoutePredicateFactory headerRoutePredicateFactory;

    @Autowired
    private QueryRoutePredicateFactory queryRoutePredicateFactory;

    @SuppressWarnings("unused")
    private List<PaymentNetwork> paymentNetworkList;

    private Map<PaymentOptionType, PaymentNetwork> paymentNetworks;

    @Autowired
    public void setPaymentNetworkList(List<PaymentNetwork> paymentNetworkList) {
        this.paymentNetworks = paymentNetworkList.stream().collect(Collectors.toMap(PaymentNetwork::getPaymentOptionType, Function.identity()));
    }

    public boolean isPaymentRequired(ServerWebExchange swe, String path, PaymentEndpoint paymentEndpoint, String price) {
        return isRequestMatching(swe, path) && (isPaymentProofMissing(swe, paymentEndpoint.getType()) || receiptNetworkVerificationFailed(swe, paymentEndpoint, price));
    }

    public boolean isPaymentRequired(ServerWebExchange swe, Api api, Route route) {
        String routeStr = route.getRoute();
        PaymentEndpoint paymentEndpoint = api.getPayment().build();
        PaymentOptionType type = paymentEndpoint.getType();
        String price = route.getPrice();

        return isRequestMatching(swe, routeStr) && (isPaymentProofMissing(swe, type) || receiptNetworkVerificationFailed(swe, paymentEndpoint, price));
    }

    private boolean isRequestMatching(ServerWebExchange swe, String path) {
        return methodMatches(swe, HttpMethod.GET) && pathMatches(swe, path);
    }

    private boolean methodMatches(ServerWebExchange swe, HttpMethod method) {
        return Objects.equals(swe.getRequest().getMethod(), method);
    }

    private boolean pathMatches(ServerWebExchange swe, String path) {
        Predicate<ServerWebExchange> routePredicate = pathRoutePredicateFactory.apply(c -> c.setPattern(path));
        return routePredicate.test(swe);
    }

    private PaymentNetwork getPaymentNetwork(PaymentOptionType type) {
        PaymentNetwork network = paymentNetworks.get(type);
        if (network == null) {
            throw new IllegalStateException("Could not get a network to support the payment option type: " + type);
        }
        return network;
    }

    private boolean isPaymentProofMissing(ServerWebExchange swe, PaymentOptionType type) {
        String pattern = getPaymentNetwork(type).getPaymentProofPattern();
        Predicate<ServerWebExchange> headerPredicate = headerRoutePredicateFactory.apply(c -> c.setHeader(RECEIPT_HEADER).setRegexp(pattern));
        Predicate<ServerWebExchange> paramPredicate = queryRoutePredicateFactory.apply(c -> c.setParam(RECEIPT_PARAM).setRegexp(pattern));
        return !headerPredicate.or(paramPredicate).test(swe);
    }

    private boolean receiptNetworkVerificationFailed(ServerWebExchange swe, PaymentEndpoint paymentEndpoint, String amount) {
        String paymentProof = Objects.requireNonNull(swe.getRequest().getHeaders().get(RECEIPT_HEADER)).stream().findFirst().orElse("");
        if (StringUtils.isBlank(paymentProof)) {
            paymentProof = Objects.requireNonNull(swe.getRequest().getQueryParams().get(RECEIPT_PARAM)).stream().findFirst().orElse("");
        }

        PaymentOptionType type = paymentEndpoint.getType();
        boolean transactionVerified = getPaymentNetwork(type).verifyTransaction(paymentProof, paymentEndpoint, amount);
        return !transactionVerified;
    }
}
