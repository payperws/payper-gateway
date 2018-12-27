package ws.payper.gateway;

import ws.payper.gateway.config.PaymentOptionType;

import java.net.URL;

public class InvoiceRequest {

    private String title;

    private URL url;

    private PaymentOptionType paymentOptionType;

    private String amount;

    private String account;

    public InvoiceRequest(String title, URL url, PaymentOptionType paymentOptionType, String amount) {
        this.title = title;
        this.url = url;
        this.paymentOptionType = paymentOptionType;
        this.amount = amount;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTitle() {
        return title;
    }

    public URL getUrl() {
        return url;
    }

    public PaymentOptionType getPaymentOptionType() {
        return paymentOptionType;
    }

    public String getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }
}
