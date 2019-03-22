package ws.payper.gateway.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("payper")
public class PaymentOptions {

    private List<PaymentOption> paymentOptions;

    public List<PaymentOption> getPaymentOptions() {
        return paymentOptions;
    }

    public void setPaymentOptions(List<PaymentOption> paymentOptions) {
        this.paymentOptions = paymentOptions;
    }
}
