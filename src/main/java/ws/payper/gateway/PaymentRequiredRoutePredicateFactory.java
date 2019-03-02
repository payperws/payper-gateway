package ws.payper.gateway;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.hedera.HederaHbarInvoicePaymentEndpoint;

import java.util.Map;
import java.util.function.Predicate;

public class PaymentRequiredRoutePredicateFactory extends AbstractRoutePredicateFactory<PaymentRequiredRoutePredicateFactory.Config> {

    private final Logger logger = LoggerFactory.getLogger(PaymentRequiredRoutePredicateFactory.class);

    @Autowired
    public PayableLinkRepository payableLinkRepository;

    @Autowired
    private InMemoryRouteDefinitionRepository routeDefinitionRepository;

    @Autowired
    private PaymentRequestVerifier paymentRequestVerifier;

    public PaymentRequiredRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(PaymentRequiredRoutePredicateFactory.Config config) {
        return swe -> {
/*
            String route = swe.getRequest().getPath().value();
            RouteDefinition routeDefinition = routeDefinitionRepository.getRouteDefinitions()
                    .filter(p -> p.getPredicates().stream().anyMatch(pd -> route.equals(pd.getArgs().get("route"))))
                    .blockFirst();

            if (routeDefinition != null) {
                PredicateDefinition predicateDefinition = routeDefinition.getPredicates().get(0);
                PaymentEndpoint paymentEndpoint = getPaymentEndpoint(predicateDefinition.getArgs());
                String price = getPrice(predicateDefinition.getArgs());
                return paymentRequestVerifier.isPaymentRequired(swe, route, paymentEndpoint, price);
            } else {
                logger.warn("Could not find route definition in repository: {}", route);
                return true;
            }
*/
            String route = swe.getRequest().getPath().value().substring(1);
            return payableLinkRepository.find(route).map(this::paymentRequired).orElse(false);
        };
    }

    private boolean paymentRequired(ConfigureLinkController.PayableLink link) {
        boolean required = true;
        return required;
    }

    private String getPrice(Map<String, String> args) {
        String price = args.get("price");
        if (StringUtils.isBlank(price)) {
            throw new IllegalStateException("price is blank");
        }
        return price;
    }

    public static PaymentEndpoint getPaymentEndpoint(Map<String, String> args) {
        String strOptionType = args.get("paymentOptionType");

        PaymentOptionType optionType = PaymentOptionType.valueOf(strOptionType);

        PaymentEndpoint endpoint;

        if (optionType == PaymentOptionType.HEDERA_HBAR_INVOICE) {
            String account = args.get("account");
            endpoint = new HederaHbarInvoicePaymentEndpoint(account);
        } else {
            throw new IllegalStateException("Payment option type not supported: " + optionType);
        }

        return endpoint;
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
