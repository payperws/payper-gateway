package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.hedera.NetworkCommunicationException;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.PayableLinkRepository;
import ws.payper.gateway.util.QrCodeGenerator;
import ws.payper.gateway.web.ConfigureLinkController;
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
    private PayableLinkRepository linkRepository;

    @Resource
    private QrCodeGenerator qrCodeGenerator;

    @Override
    public Invoice newInvoice(InvoiceRequest invoiceRequest) {
        PaymentOptionType paymentOptionType = invoiceRequest.getPaymentOptionType();
        String amount = invoiceRequest.getAmount();

        String contentDescription = MessageFormat.format("{0} [ {1} ]", invoiceRequest.getTitle(), invoiceRequest.getUrl());

        LightningPaymentNetwork network = connectToNetwork(invoiceRequest.getPayableLinkId());

        AddInvoiceResponse invoiceResponse = generateInvoice(network, amount, contentDescription);
        Map<String, String> params = new HashMap<>();
        params.put("content_title", contentDescription);
        params.put("url", invoiceRequest.getUrl().toString());
        params.put("amount", amount);
        params.put("qr_code", getQrCode(invoiceResponse.getPaymentRequest()));
        params.put("pay_req", invoiceResponse.getPaymentRequest());
        String rhash = new String(Base64.getEncoder().encode(invoiceResponse.getRHash()));
        params.put("r_hash", rhash);
        params.put("invoice_id", rhash);
        return new Invoice(invoiceRequest.getPayableLinkId(), paymentOptionType, amount, params);
    }

    private String getQrCode(String paymentRequest) {
        return qrCodeGenerator.base64encoded(paymentRequest);
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.LIGHTNING_BTC;
    }

    private AddInvoiceResponse generateInvoice(LightningPaymentNetwork network, String amount, String memo) {
        Long amountLong = Long.parseLong(amount);
        return network.addInvoice(amountLong, memo);
    }

    private LightningPaymentNetwork connectToNetwork(String payableLinkId) {
        ConfigureLinkController.PayableLink link = linkRepository.find(payableLinkId)
                .orElseThrow(() -> new IllegalStateException("Could not find link: " + payableLinkId));

        // TODO get from repo
        String host = "212.47.234.65";
        int port = 10009;
        File tlsCert = new File("/home/alex/.lnd/tls.cert");;
        File invoiceMacaroon = new File("/home/alex/.lnd/data/chain/bitcoin/testnet/invoice.macaroon");;
        SynchronousLndAPI api;
        try {
            api = new SynchronousLndAPI(host, port, tlsCert, invoiceMacaroon);
        } catch (SSLException | ClientSideException e) {
            throw new NetworkCommunicationException("Could not connect to network for link: " + payableLinkId, e);
        }
        return new LightningPaymentNetwork(api);
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
