package ws.payper.gateway.dummy;

import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DummyCoinPaymentNetwork implements PaymentNetwork {

    private final ConcurrentMap<String, Boolean> paidInvoices = new ConcurrentHashMap<>();

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.DUMMY_COIN;
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        synchronized (paidInvoices) {
            return paidInvoices.getOrDefault(paymentProof, false);
        }
    }

    public void addInvoice(String invoiceId) {
        paidInvoices.putIfAbsent(invoiceId, false);
    }

    @Override
    public String getPaymentProofPattern() {
        return "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }

    public void payInvoice(String invoice) {
        paidInvoices.replace(invoice, true);
    }
}
