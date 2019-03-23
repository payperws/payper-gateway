package ws.payper.gateway.dummy;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.util.PaymentUriHelper;
import ws.payper.gateway.util.QrCodeGenerator;
import ws.payper.gateway.web.ConfigureLinkController;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

@Component
public class DummyCoinInvoiceGenerator implements InvoiceGenerator {

    @Autowired
    private QrCodeGenerator qrCodeGenerator;

    @Autowired
    private PaymentUriHelper paymentUriHelper;

    @Autowired
    private DummyCoinPaymentNetwork network;

    @Override
    public Invoice newInvoice(PayableLink link) {
        String invoiceId = UUID.randomUUID().toString();
        String dummyWalletUrl = dummyWalletUrl(link, invoiceId);
        ConfigureLinkController.LinkConfig linkConfig = link.getLinkConfig();
        Map<String, String> args = Map.of(
                "content_title", linkConfig.getTitle(),
                "url", linkConfig.getUrl(),
                "amount", linkConfig.getPrice().toString(),
                "currency", linkConfig.getCurrency().name(),
                "invoice_id", invoiceId,
                "qr_code", getQrCode(dummyWalletUrl),
                "open_in_wallet", dummyWalletUrl
        );

        Invoice invoice = new Invoice(invoiceId, link, args);

        network.addInvoice(invoiceId);

        return invoice;
    }

    private String dummyWalletUrl(PayableLink link, String invoiceId) {
        try {
            ConfigureLinkController.LinkConfig linkConfig = link.getLinkConfig();
            return new URIBuilder(paymentUriHelper.getBaseUrl() + "/dummy-wallet")
                    .addParameter("amount", linkConfig.getPrice().toString())
                    .addParameter("invoice", invoiceId)
                    .addParameter("note", "Payment for: " + linkConfig.getTitle())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.DUMMY_COIN;
    }

    private String getQrCode(String paymentRequest) {
        return qrCodeGenerator.base64encoded(paymentRequest);
    }
}
