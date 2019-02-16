package ws.payper.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

public class PaymentRequiredRoutePredicateFactory extends AbstractRoutePredicateFactory<PaymentRequiredRoutePredicateFactory.Config> {

    @Autowired
    private PaymentRequestVerifier paymentRequestVerifier;

    public PaymentRequiredRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(PaymentRequiredRoutePredicateFactory.Config config) {
        return swe -> {
/* TODO
            String route = "";
            PaymentEndpoint paymentEndpoint = api.getPayment().build();
            PaymentOptionType type = paymentEndpoint.getType();
            String price = route.getPrice();

            return paymentRequestVerifier.isPaymentRequired(swe, api, route);
*/
            return true;
        };
    }

    @Validated
    public static class Config {

        private String pattern;

        public String getPattern() {
            return pattern;
        }

        public PaymentRequiredRoutePredicateFactory.Config setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this)
                    .append("pattern", pattern)
                    .toString();
        }
    }
}
