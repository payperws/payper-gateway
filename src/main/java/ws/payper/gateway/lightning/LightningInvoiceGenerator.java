package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.springframework.stereotype.Component;
import ws.payper.gateway.Invoice;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.InvoiceRequest;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.util.QrCodeGenerator;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class LightningInvoiceGenerator implements InvoiceGenerator {

    @Resource
    private LightningPaymentNetwork network;

    @Resource
    private QrCodeGenerator qrCodeGenerator;

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
        String rhash = new String(Base64.getEncoder().encode(invoiceResponse.getRHash()));
        params.put("r_hash", rhash);
        params.put("invoice_id", rhash);
        return new Invoice(paymentOptionType, amount, params);
    }

    private String getQrCode(String paymentRequest) {
        return qrCodeGenerator.base64encoded(paymentRequest);
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
