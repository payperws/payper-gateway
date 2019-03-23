package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.PayableLinkRepository;
import ws.payper.gateway.util.QrCodeGenerator;
import ws.payper.gateway.web.InvoiceRequest;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class LightningInvoiceGenerator implements InvoiceGenerator {

    @Resource
    private LightningPaymentNetwork network;

    @Resource
    private PayableLinkRepository linkRepository;

    @Resource
    private QrCodeGenerator qrCodeGenerator;

    @Override
    public Invoice newInvoice(PayableLink link) {
        String linkId = link.getPayableId();
        String amount = link.getLinkConfig().getPrice().toString();

        AddInvoiceResponse invoiceResponse = generateInvoice(linkId, amount, link.getPayableUrl());
        Map<String, String> params = new HashMap<>();
        params.put("pay_req_qr_code", getQrCode(invoiceResponse.getPaymentRequest()));
        params.put("pay_req", invoiceResponse.getPaymentRequest());
        String rhash = new String(Base64.getEncoder().encode(invoiceResponse.getRHash()));
        params.put("r_hash", rhash);
        return new Invoice(rhash, link, params);
    }

    private String getQrCode(String paymentRequest) {
        return qrCodeGenerator.base64encoded(paymentRequest);
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.LIGHTNING_BTC;
    }

    private AddInvoiceResponse generateInvoice(String payableLinkId, String amount, String memo) {
        Long amountLong = Long.parseLong(amount);
        return network.addInvoice(payableLinkId, amountLong, memo);
    }

    public static void main(String[] args) throws StatusException, ValidationException, SSLException {
        File macaroonFile = new File("/home/alex/.lnd/data/chain/bitcoin/testnet/invoice.macaroon");
        File certFile = new File("/home/alex/.lnd/tls.cert");
        SynchronousLndAPI lndAPI = new SynchronousLndAPI("212.47.234.65", 10009, certFile, macaroonFile);
        org.lightningj.lnd.wrapper.message.Invoice invoice = new org.lightningj.lnd.wrapper.message.Invoice();
        invoice.setMemo("X1-" + StringUtils.randomAlphanumeric(5));
        invoice.setValue(500);
        AddInvoiceResponse response = lndAPI.addInvoice(invoice);

        System.out.println(response.getPaymentRequest());
    }

}
