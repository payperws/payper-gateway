package ws.payper.gateway.dummy;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.web.InvoiceRequest;
import ws.payper.gateway.util.PaymentUriHelper;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.util.QrCodeGenerator;

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
    public Invoice newInvoice(InvoiceRequest request) {
        String invoiceId = UUID.randomUUID().toString();
        String dummyWalletUrl = dummyWalletUrl(request, invoiceId);
        Map<String, String> args = Map.of(
                "content_title", request.getTitle(),
                "url", request.getUrl().toString(),
                "amount", request.getAmount(),
                "currency", request.getCurrency(),
                "invoice_id", invoiceId,
                "qr_code", getQrCode(dummyWalletUrl),
                "open_in_wallet", dummyWalletUrl
        );

        Invoice invoice = new Invoice(request.getPayableLinkId(), request.getPaymentOptionType(), request.getAmount(), args);

        network.addInvoice(invoiceId);

        return invoice;
    }

    private String dummyWalletUrl(InvoiceRequest request, String invoiceId) {
        try {
            return new URIBuilder(paymentUriHelper.getBaseUrl() + "/dummy-wallet")
                    .addParameter("amount", request.getAmount())
                    .addParameter("invoice", invoiceId)
                    .addParameter("note", "Payment for: " + request.getTitle())
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
