package ws.payper.gateway.dummy;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public class DummyCoinPaymentEndpoint extends PaymentEndpoint {

    public DummyCoinPaymentEndpoint() {
        super(PaymentOptionType.DUMMY_COIN);
    }
}
