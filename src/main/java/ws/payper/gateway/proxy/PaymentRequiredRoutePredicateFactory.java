package ws.payper.gateway.proxy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.PaymentRequestVerifier;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.dummy.DummyCoinPaymentEndpoint;
import ws.payper.gateway.hedera.HederaHbarInvoicePaymentEndpoint;
import ws.payper.gateway.repo.PayableLinkRepository;
import ws.payper.gateway.util.PaymentUriHelper;

import java.util.function.Predicate;

public class PaymentRequiredRoutePredicateFactory extends AbstractRoutePredicateFactory<PaymentRequiredRoutePredicateFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger("PaymentProxy");

    @Autowired
    public PayableLinkRepository payableLinkRepository;

    @Autowired
    private InMemoryRouteDefinitionRepository routeDefinitionRepository;

    @Autowired
    private PaymentRequestVerifier paymentRequestVerifier;

    @Autowired
    private PaymentUriHelper paymentUriHelper;

    public PaymentRequiredRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(PaymentRequiredRoutePredicateFactory.Config config) {
        return swe -> {
            String route = swe.getRequest().getPath().value();
            if (paymentUriHelper.isPayableLinkPath(route)) {
                String payableId = paymentUriHelper.extractPayableId(route);
                if (StringUtils.isNotBlank(payableId) && payableId.equals(config.getLinkId())) {
                    return payableLinkRepository.findByPayableId(payableId).map(link -> paymentRequired(link, swe)).orElse(false);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        };
    }

    private boolean paymentRequired(PayableLink link, ServerWebExchange swe) {
        PaymentOptionType type = link.getLinkConfig().getPaymentOptionType();
        PaymentEndpoint paymentEndpoint;
        if (PaymentOptionType.DUMMY_COIN.equals(type)) {
            paymentEndpoint = new DummyCoinPaymentEndpoint();
        } else {
            String account = link.getLinkConfig().getPaymentOptionArgs().get("account");
            paymentEndpoint = new HederaHbarInvoicePaymentEndpoint(account);
        }
        boolean paymentRequired = paymentRequestVerifier.isPaymentRequired(swe, link.getPayablePath(), paymentEndpoint, link.getLinkConfig().getPrice().toString());
        return paymentRequired;
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Validated
    public static class Config {

        private String linkId;

        public String getLinkId() {
            return linkId;
        }

        public PaymentRequiredRoutePredicateFactory.Config setLinkId(String linkId) {
            this.linkId = linkId;
            return this;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this)
                    .append("linkId", linkId)
                    .toString();
        }
    }
}
