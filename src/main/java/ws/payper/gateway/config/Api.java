package ws.payper.gateway.config;

import java.util.ArrayList;
import java.util.List;

public class Api {

    private String name;

    private String baseUrl;

    private PaymentOptionBuilder payment;

    private List<Route> routes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public PaymentOptionBuilder getPayment() {
        return payment;
    }

    public void setPayment(PaymentOptionBuilder payment) {
        this.payment = payment;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
