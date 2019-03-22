package ws.payper.gateway.service;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PaymentOptionsService {

    @Resource
    private PaymentOptions paymentOptions;

    public PaymentOptions availablePaymentOptions() {
        return paymentOptions;
    }
}
