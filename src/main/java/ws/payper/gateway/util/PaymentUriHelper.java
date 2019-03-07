package ws.payper.gateway.util;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.web.ConfigureLinkController;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PaymentUriHelper {

    @Value("${payper.baseUrl}")
    private String baseUrl;

    @Value("${payper.redirectPath}")
    private String redirectPath;

    private String payableBaseUrl;

    private String paymentRequiredBaseUrl;

    @PostConstruct
    public void init() {
        this.payableBaseUrl = baseUrl + "/pl";
        this.paymentRequiredBaseUrl = baseUrl + "/" + redirectPath;
    }

    public URI payableUri(String payableId) {
        return URI.create(payableBaseUrl + "/" + payableId);
    }

    public URI paymentRequiredUri(ConfigureLinkController.PayableLink payable) {
        ConfigureLinkController.LinkConfig linkConfig = payable.getLinkConfig();

        PaymentOptionType paymentOptionType = linkConfig.getPaymentOptionType();

        String payableLinkId = payable.getPayableId();
        String title = linkConfig.getTitle();
        String sourceUrl = payable.getPayableUrl();
        String paymentOption = paymentOptionType.name();
        String amount = linkConfig.getPrice().toString();
        String currency = linkConfig.getCurrency().name();

        try {

            URIBuilder uriBuilder = new URIBuilder(paymentRequiredBaseUrl)
                    .addParameter("payableLinkId", payableLinkId)
                    .addParameter("title", title)
                    .addParameter("sourceurl", sourceUrl)
                    .addParameter("option", paymentOption)
                    .addParameter("amount", amount)
                    .addParameter("currency", currency);

            if (PaymentOptionType.HEDERA_HBAR.equals(paymentOptionType)) {
                String account = linkConfig.getPaymentOptionArgs().get("account");
                uriBuilder.addParameter("account", account);
            } else if (PaymentOptionType.HEDERA_HBAR_INVOICE.equals(paymentOptionType)) {
                String account = linkConfig.getPaymentOptionArgs().get("account");
                uriBuilder.addParameter("account", account);
            }

            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not build redirect URL", e);
        }
    }

    public boolean isPayableLinkPath(String route) {
        return route.startsWith("/pl/");
    }

    public String extractPayableId(String route) {
        return route.substring(4);
    }

    public String payablePath(String payableId) {
        return "/pl/" + payableId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
