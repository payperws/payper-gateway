package ws.payper.gateway.hedera;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public class HederaHbarInvoicePaymentEndpoint extends PaymentEndpoint {

    private String account;

    public HederaHbarInvoicePaymentEndpoint(String account) {
        super(PaymentOptionType.HEDERA_HBAR_INVOICE);
        this.account = account;
    }

    public String getAccount() {
        return account;
    }
}
