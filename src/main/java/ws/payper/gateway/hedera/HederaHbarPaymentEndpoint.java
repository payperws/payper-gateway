package ws.payper.gateway.hedera;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public class HederaHbarPaymentEndpoint extends PaymentEndpoint {

    private String account;

    public HederaHbarPaymentEndpoint(String account) {
        super(PaymentOptionType.HEDERA_HBAR);
        this.account = account;
    }

    public String getAccount() {
        return account;
    }
}
