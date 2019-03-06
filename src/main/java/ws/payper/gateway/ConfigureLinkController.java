package ws.payper.gateway;


import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ws.payper.gateway.config.PaymentOptionType;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_HEADER;

@Controller
public class ConfigureLinkController {

    @Autowired
    private PayableLinkRepository repository;

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PaymentUriHelper uriBuilder;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showNewLinkPage() {
        return "configure-link";
    }

    @RequestMapping(value = "/ok", method = RequestMethod.GET)
    public String linkCreated(@RequestParam String url, Model model) {
        model.addAttribute("payableLink", url);
        return "link-created";
    }

    @PostMapping(value = "/link")
    @ResponseBody
    public
    Mono<PayableLink> newLink(@RequestBody LinkConfig link) {
        String payableId = RandomStringUtils.randomAlphanumeric(10);
        String payableUrl = uriBuilder.payableUri(payableId).toString();
        String payablePath = uriBuilder.payablePath(payableId);
        PayableLink payable = new PayableLink(link, payableId, payableUrl, payablePath);

        RouteDefinition redirectDef = createRedirectRouteDefinition(payable);
        RouteDefinition defaultDef = createDefaultRouteDefinition(payable);

        return save(payableId, payable, Mono.just(redirectDef), Mono.just(defaultDef));
    }

    private RouteDefinition createRedirectRouteDefinition(PayableLink payable) {
        RouteDefinition route = new RouteDefinition();

        route.setUri(URI.create(payable.getLinkConfig().getUrl()));
        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("PaymentRequired");
        predicateDefinition.setArgs(Map.of("linkId", payable.getPayableId()));
        route.setPredicates(List.of(predicateDefinition));

        FilterDefinition filterDef = new FilterDefinition();
        filterDef.setName("RedirectTo");
        String redirectUrl = uriBuilder.paymentRequiredUri(payable).toString();
        Map<String, String> filterArgs = Map.of("status", "302", "url", redirectUrl);
        filterDef.setArgs(filterArgs);
        route.setFilters(List.of(filterDef));

        return route;
    }

    private RouteDefinition createDefaultRouteDefinition(PayableLink payable) {
        RouteDefinition route = new RouteDefinition();

        route.setUri(URI.create(payable.getLinkConfig().getUrl()));

        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("HeaderOrParam");
        predicateDefinition.setArgs(Map.of("linkId", payable.getPayableId()));
        route.setPredicates(List.of(predicateDefinition));

        FilterDefinition removeHeaderFilter = new FilterDefinition();
        removeHeaderFilter.setName("RemoveRequestHeader");
        removeHeaderFilter.setArgs(Map.of("name", RECEIPT_HEADER));

        // TODO remove query parameter

        route.setFilters(List.of(removeHeaderFilter));
        return route;
    }

    public Mono<PayableLink> save(String id, PayableLink link, Mono<RouteDefinition> routeDef, Mono<RouteDefinition> defaultDef) {
        return this.routeDefinitionWriter.save(routeDef.map(r -> {
                    r.setId(id + "-redirect");
                    return r;
                }))
                .then(this.routeDefinitionWriter.save(defaultDef.map(r -> {
                    r.setId(id + "-default");
                    return r;
                })))
                .then(Mono.defer(this::refresh))
                .then(Mono.defer(() ->
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

        private String title;

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

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class PayableLink {

        private LinkConfig linkConfig;

        private String payableId;

        private String payableUrl;

        private String payablePath;

        public PayableLink(LinkConfig linkConfig, String payableId, String payableUrl, String payablePath) {
            this.linkConfig = linkConfig;
            this.payableId = payableId;
            this.payableUrl = payableUrl;
            this.payablePath = payablePath;
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

        public String getPayablePath() {
            return payablePath;
        }
    }
}
