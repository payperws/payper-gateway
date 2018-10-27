package ws.payper.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("price-list")
public class RoutePriceConfiguration {

    private List<Api> apis = new ArrayList<>();

    public List<Api> getApis() {
        return apis;
    }

}
