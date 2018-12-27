package ws.payper.gateway;

import ws.payper.gateway.config.PaymentOptionType;

import java.util.HashMap;
import java.util.Map;

public class Invoice {

    private PaymentOptionType paymentOptionType;

    private String price;

    private Map<String, String> paymentOptionParameters;

    public Invoice(PaymentOptionType paymentOptionType, String price, Map<String, String> paymentOptionParameters) {
        this.paymentOptionType = paymentOptionType;
        this.price = price;
        this.paymentOptionParameters = paymentOptionParameters;
    }

    public PaymentOptionType getPaymentOptionType() {
        return paymentOptionType;
    }

    public void setPaymentOptionType(PaymentOptionType paymentOptionType) {
        this.paymentOptionType = paymentOptionType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Map<String, String> getPaymentOptionParameters() {
        return paymentOptionParameters;
    }

    public void setPaymentOptionParameters(Map<String, String> paymentOptionParameters) {
        this.paymentOptionParameters = paymentOptionParameters;
    }

    public Map<String, String> allParameters() {
        Map<String, String> allParams = new HashMap<>(paymentOptionParameters);
        allParams.put("paymentOptionType", paymentOptionType.name());
        allParams.put("price", price);
        return allParams;
    }
}
