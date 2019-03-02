package ws.payper.gateway;


import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ws.payper.gateway.config.PaymentOptionType;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
public class ConfigureLinkController {

    @Autowired
    private PayableLinkRepository repository;

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping(value = "/link")
    public
    Mono<PayableLink> newLink(@RequestBody LinkConfig link) {
        String payableId = RandomStringUtils.randomAlphanumeric(10);
        String payableStr = "http://localhost:8080/" + payableId;
        PayableLink payable = new PayableLink(link, payableId, payableStr);

        RouteDefinition routeDefinition = createRouteDefinition(payable);

        return save(payableId, payable, Mono.just(routeDefinition));

//        return repository.save(payable);
    }

    private RouteDefinition createRouteDefinition(PayableLink payable) {
        RouteDefinition route = new RouteDefinition();

        route.setUri(URI.create(payable.getLinkConfig().getUrl()));
        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("PaymentRequired");
        route.setPredicates(List.of(predicateDefinition));

        FilterDefinition filterDef = new FilterDefinition();
        filterDef.setName("RedirectTo");
        String redirectUrl = "http://localhost:8080/pypr/payment-required?title=Top+Ten+Myths+of+Distributed+Ledger+Technologies&sourceurl=http%3A%2F%2Flocalhost%3A8080%2Ftop10myths&option=HEDERA_HBAR_INVOICE&amount=500000000&account=0.0.1209";
        Map<String, String> filterArgs = Map.of("status", "302", "url", redirectUrl);
        filterDef.setArgs(filterArgs);
        route.setFilters(List.of(filterDef));

        return route;
    }


    public Mono<PayableLink> save(String id, PayableLink link, Mono<RouteDefinition> route) {
        return this.routeDefinitionWriter.save(route.map(r -> {
                    r.setId(id);
                    return r;
                }
        )).then(Mono.defer(this::refresh
        )).then(Mono.defer(() ->
                Mono.just(repository.save(link))));
    }

    public Mono<Void> refresh() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return Mono.empty();
    }

    public static class LinkConfig {

        private String url;

        private PaymentOptionType paymentOptionType;

        private Map<String, String> paymentOptionArgs;

        private BigDecimal price;

        private CryptoCurrency currency;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public PaymentOptionType getPaymentOptionType() {
            return paymentOptionType;
        }

        public void setPaymentOptionType(PaymentOptionType paymentOptionType) {
            this.paymentOptionType = paymentOptionType;
        }

        public Map<String, String> getPaymentOptionArgs() {
            return paymentOptionArgs;
        }

        public void setPaymentOptionArgs(Map<String, String> paymentOptionArgs) {
            this.paymentOptionArgs = paymentOptionArgs;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public CryptoCurrency getCurrency() {
            return currency;
        }

        public void setCurrency(CryptoCurrency currency) {
            this.currency = currency;
        }
    }

    public static class PayableLink {

        private LinkConfig linkConfig;

        private String payableId;

        private String payableUrl;

        public PayableLink(LinkConfig linkConfig, String payableId, String payableUrl) {
            this.linkConfig = linkConfig;
            this.payableId = payableId;
            this.payableUrl = payableUrl;
        }

        public LinkConfig getLinkConfig() {
            return linkConfig;
        }

        public String getPayableId() {
            return payableId;
        }

        public String getPayableUrl() {
            return payableUrl;
        }
    }
}
