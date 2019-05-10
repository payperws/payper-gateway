package ws.payper.gateway.hedera;

import org.springframework.stereotype.Component;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.web.InvoiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class HederaInvoiceGenerator implements InvoiceGenerator {

    @Override
    public Invoice newInvoice(PayableLink link) {
        String invoiceId = UUID.randomUUID().toString();
        String account = "unknown-account";// link.getLinkConfig().getAccount();

        Map<String, String> params = new HashMap<>();
        params.put("account", account);
        return new Invoice(invoiceId, link, params);
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR;
    }
}
