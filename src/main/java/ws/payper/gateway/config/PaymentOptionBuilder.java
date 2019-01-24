package ws.payper.gateway.config;

import ws.payper.gateway.hedera.HederaHbarInvoicePaymentEndpoint;
import ws.payper.gateway.hedera.HederaHbarPaymentEndpoint;
import ws.payper.gateway.lightning.LightningBtcPaymentEndpoint;

import java.util.Objects;


public class PaymentOptionBuilder {

    private PaymentOptionType type;

    private String account;

    public PaymentEndpoint build() {
        if (PaymentOptionType.LIGHTNING_BTC.equals(type)) {
            return new LightningBtcPaymentEndpoint();
        } else if (PaymentOptionType.HEDERA_HBAR.equals(type)){
            validateHederaParams(account);
            return new HederaHbarPaymentEndpoint(account);
        } else if (PaymentOptionType.HEDERA_HBAR_INVOICE.equals(type)){
            validateHederaParams(account);
            return new HederaHbarInvoicePaymentEndpoint(account);
        } else {
            throw new ConfigurationException("Unexpected payment option type: " + type);
        }
    }

    private void validateHederaParams(String account) {
        Objects.requireNonNull(account, "paymentRequestString must not be null!");
    }

    public void setType(PaymentOptionType type) {
        this.type = type;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
