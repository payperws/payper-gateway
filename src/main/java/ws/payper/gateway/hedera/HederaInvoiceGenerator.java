package ws.payper.gateway.hedera;

import org.springframework.stereotype.Component;
import ws.payper.gateway.Invoice;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.InvoiceRequest;
import ws.payper.gateway.config.PaymentOptionType;

import java.util.HashMap;
import java.util.Map;

@Component
public class HederaInvoiceGenerator implements InvoiceGenerator {

    @Override
    public Invoice newInvoice(InvoiceRequest invoiceRequest) {
        PaymentOptionType paymentOptionType = invoiceRequest.getPaymentOptionType();
        String amount = invoiceRequest.getAmount();
        String account = invoiceRequest.getAccount();

        Map<String, String> params = new HashMap<>();
        params.put("account", account);
        return new Invoice(invoiceRequest.getPayableLinkId(), paymentOptionType, amount, params);
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR;
    }
}
