package ws.payper.gateway.config;

public abstract class PaymentEndpoint {

    private PaymentOptionType type;

    public PaymentEndpoint(PaymentOptionType type) {
        this.type = type;
    }

    public PaymentOptionType getType() {
        return type;
    }
}
