package ws.payper.gateway.model;

import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;

import java.util.HashMap;
import java.util.Map;

public class Invoice {

    private String invoiceId;

    private PayableLink payableLink;

    private Map<String, String> paymentOptionParameters;

    public Invoice() {
    }

    public Invoice(String invoiceId, PayableLink link, Map<String, String> params) {
        this.invoiceId = invoiceId;
        this.payableLink = link;
        this.paymentOptionParameters = params;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public PayableLink getPayableLink() {
        return payableLink;
    }

    public void setPayableLink(PayableLink payableLink) {
        this.payableLink = payableLink;
    }

    public Map<String, String> getPaymentOptionParameters() {
        return paymentOptionParameters;
    }

    public void setPaymentOptionParameters(Map<String, String> paymentOptionParameters) {
        this.paymentOptionParameters = paymentOptionParameters;
    }
}
