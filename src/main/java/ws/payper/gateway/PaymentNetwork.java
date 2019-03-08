package ws.payper.gateway;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public interface PaymentNetwork {

    PaymentOptionType getPaymentOptionType();

    boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount);

    String getPaymentProofPattern();
}
