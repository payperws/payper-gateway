package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.springframework.stereotype.Component;
import ws.payper.gateway.Invoice;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.InvoiceRequest;
import ws.payper.gateway.config.PaymentOptionType;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class LightningInvoiceGenerator implements InvoiceGenerator {

    @Resource
    private LightningPaymentNetwork network;

    @Override
    public Invoice newInvoice(InvoiceRequest invoiceRequest) {
        PaymentOptionType paymentOptionType = invoiceRequest.getPaymentOptionType();
        String amount = invoiceRequest.getAmount();

        String contentDescription = MessageFormat.format("{0} [ {1} ]", invoiceRequest.getTitle(), invoiceRequest.getUrl());
        AddInvoiceResponse invoiceResponse = generateInvoice(amount, contentDescription);
        Map<String, String> params = new HashMap<>();
        params.put("content_title", contentDescription);
        params.put("url", invoiceRequest.getUrl().toString());
        params.put("amount", amount);
        params.put("qr_code", getQrCode(invoiceResponse.getPaymentRequest()));
        params.put("pay_req", invoiceResponse.getPaymentRequest());
        params.put("r_hash", new String (Base64.getEncoder().encode(invoiceResponse.getRHash())));
        return new Invoice(paymentOptionType, amount, params);
    }

    private String getQrCode(String paymentRequest) {
        return "iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAABZUlEQVR42u2YQY7DIAxFYZVjcFMSbsoxWMH8/4EmrTSb0Uj2oshqIz8WKLa/TcL4bYUv+SNpASuOerYAy8e46Eg+SMIRy4AjwQeCZzp9EPhKC5H2evZERg0HjEf2R8a04Ywo2vWUZe0aXoiqpCHIyz7qx5LMtTKRVfKhO5ZEDhzzWroCpVHYPRCtPslRsYthb8kFwQ9OfSgTKcys5uiGFAW506bAYNdwQfRfePbUmYY1vt61OWlssxl9THZKbLjFB8GpkYN5aXNFxfSlO/YkSo+nuoTnFGBPkgqXFTxj/pierIkkmX1sz3TloTvGZE92Q+oiSa7RCVGJnGOpC7uZ9rogUj6qMh08OEN9q7IpWVMAExCwy12ckD09XTsH76nTnigTZ7+dqty28jkg82aTZ499ywMfROpSVutAW/NE9kB3qd++3xstyYz2RV3Be2XLXVOAPblvOZ39nyt/3n+MyPd71f+SH8TnCmM/Ep27AAAAAElFTkSuQmCC";
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.LIGHTNING_BTC;
    }

    private AddInvoiceResponse generateInvoice(String amount, String memo) {
        Long amountLong = Long.parseLong(amount);
        return network.addInvoice(amountLong, memo);
    }
}
