package ws.payper.gateway;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;
import ws.payper.gateway.config.Api;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.config.Route;
import ws.payper.gateway.hedera.HederaHbarPaymentEndpoint;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PaymentUriBuilder {

    public URI buildUri(String paymentRequiredUrl, String sourceUrl, Api api, Route route) {
        try {
            PaymentEndpoint endpoint = api.getPayment().build();

            URIBuilder uriBuilder = new URIBuilder(paymentRequiredUrl)
                    .addParameter("title", route.getTitle())
                    .addParameter("sourceurl", sourceUrl)
                    .addParameter("option", endpoint.getType().name())
                    .addParameter("amount", route.getPrice());

            if (PaymentOptionType.HEDERA_HBAR.equals(endpoint.getType())) {
                uriBuilder.addParameter("account", ((HederaHbarPaymentEndpoint) endpoint).getAccount());
            }

            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not build redirect URL", e);
        }
    }
}
