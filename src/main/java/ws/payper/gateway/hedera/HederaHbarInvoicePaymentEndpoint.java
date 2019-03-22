package ws.payper.gateway.hedera;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public class HederaHbarInvoicePaymentEndpoint extends PaymentEndpoint {

    public HederaHbarInvoicePaymentEndpoint() {
        super(PaymentOptionType.HEDERA_HBAR_INVOICE);
    }
}
