package ws.payper.gateway.proxy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import ws.payper.gateway.repo.InvoiceRepository;

import java.util.function.Predicate;

import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_HEADER;
import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_PARAM;

public class HeaderOrParamRoutePredicateFactory extends AbstractRoutePredicateFactory<HeaderOrParamRoutePredicateFactory.Config> {

    private final Logger log = LoggerFactory.getLogger("PaymentProxy");

    @Autowired
    private InvoiceRepository invoiceRepository;

    public HeaderOrParamRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return swe -> {
            ServerHttpRequest request = swe.getRequest();
            String receiptHeader = request.getHeaders().getFirst(RECEIPT_HEADER);
            String receiptParam = request.getQueryParams().getFirst(RECEIPT_PARAM);

            String invoiceId = getFirstNonBlank(receiptHeader, receiptParam);

            if (invoiceId != null) {
                return invoiceRepository.find(invoiceId).map(invoice -> {
                    boolean found = invoice.getPayableLinkId().equals(config.getLinkId());
                    if (found) {
                        log.info("[{}] [{}] invoice paid - proxying request", invoice.getPayableLinkId(), invoiceId);
                    }
                    return found;
                }).orElse(false);
            } else {
                return false;
            }
        };
    }

    private String getFirstNonBlank(String receiptHeader, String receiptParam) {
        String result = null;
        if (StringUtils.isNotBlank(receiptHeader)) {
            result = receiptHeader;
        } else if (StringUtils.isNotBlank(receiptParam)) {
            result = receiptParam;
        }
        return result;
    }

    public boolean isSame(String linkId, String configuredLinkId) {
        return (StringUtils.isNotBlank(linkId)) && (linkId.equals(configuredLinkId));
    }

    @Validated
    public static class Config {

        private String linkId;

        public String getLinkId() {
            return linkId;
        }

        public HeaderOrParamRoutePredicateFactory.Config setLinkId(String linkId) {
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
