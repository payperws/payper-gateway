package ws.payper.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.repo.PayableLinkRepository;
import ws.payper.gateway.util.PaymentUriHelper;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ws.payper.gateway.PaymentRequestVerifier.RECEIPT_HEADER;

@Component
public class RouteService {

    @Autowired
    private PaymentUriHelper uriBuilder;

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PayableLinkRepository repository;

    public void preload() {
        List<PayableLink> links = repository.findAll();
        register(links).then(refresh()).block();
    }

    private Mono<Void> register(List<PayableLink> link) {
        Iterator<PayableLink> it = link.iterator();

        Mono<Void> prevMono = Mono.empty();
        while(it.hasNext()) {
            prevMono = register(it.next(), prevMono);
        }

        return prevMono;
    }

    private Mono<Void> register(PayableLink link, Mono<Void> prevMono) {
        String id = link.getPayableId();

        Mono<RouteDefinition> redirectDef = Mono.just(createRedirectRouteDefinition(link));
        Mono<RouteDefinition> defaultDef = Mono.just(createDefaultRouteDefinition(link));

        return prevMono.then(this.routeDefinitionWriter.save(redirectDef.map(r -> {
            r.setId(id + "-redirect");
            return r;
        })))
                .then(this.routeDefinitionWriter.save(defaultDef.map(r -> {
                    r.setId(id + "-default");
                    return r;
                })));
    }

    private Mono<Void> refresh() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return Mono.empty();
    }

    public Mono<PayableLink> registerAndRefresh(PayableLink link) {
        RouteDefinition redirectDef = createRedirectRouteDefinition(link);
        RouteDefinition defaultDef = createDefaultRouteDefinition(link);

        return saveAndRefresh(link.getPayableId(), link, Mono.just(redirectDef), Mono.just(defaultDef));
    }

    private Mono<PayableLink> saveAndRefresh(String id, PayableLink link, Mono<RouteDefinition> routeDef, Mono<RouteDefinition> defaultDef) {
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


    private RouteDefinition createRedirectRouteDefinition(PayableLink payable) {
        RouteDefinition route = new RouteDefinition();

        route.setUri(URI.create(payable.getLinkConfig().getUrl()));
        PredicateDefinition predicateDefinition = new PredicateDefinition();
        predicateDefinition.setName("PaymentRequired");
        predicateDefinition.setArgs(Map.of("linkId", payable.getPayableId()));
        route.setPredicates(List.of(predicateDefinition));

        FilterDefinition filterDef = new FilterDefinition();
        filterDef.setName("CustomRedirectTo");
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

}
