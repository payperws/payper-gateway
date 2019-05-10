package ws.payper.gateway.lightning;

import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

public class LightningBtcPaymentEndpoint extends PaymentEndpoint {

    public LightningBtcPaymentEndpoint() {
        super(PaymentOptionType.LIGHTNING_BTC);
    }
}
