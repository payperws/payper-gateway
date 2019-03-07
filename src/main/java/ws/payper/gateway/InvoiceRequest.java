package ws.payper.gateway;

import ws.payper.gateway.config.PaymentOptionType;

import java.net.URL;

public class InvoiceRequest {

    private String title;

    private URL url;

    private PaymentOptionType paymentOptionType;

    private String amount;

    private String account;

    private final String currency;

    private String payableLinkId;

    public InvoiceRequest(String payableLinkId, String title, URL url, PaymentOptionType paymentOptionType, String amount, String currency) {
        this.payableLinkId = payableLinkId;
        this.title = title;
        this.url = url;
        this.paymentOptionType = paymentOptionType;
        this.amount = amount;
        this.currency = currency;
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

    public String getCurrency() {
        return currency;
    }

    public String getAccount() {
        return account;
    }

    public String getPayableLinkId() {
        return payableLinkId;
    }

    public void setPayableLinkId(String payableLinkId) {
        this.payableLinkId = payableLinkId;
    }
}
