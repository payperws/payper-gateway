package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.lightningj.lnd.wrapper.message.PaymentHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.hedera.NetworkCommunicationException;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Base64;

@Component
public class LightningPaymentNetwork implements PaymentNetwork {

    private final Logger log = LoggerFactory.getLogger(LightningPaymentNetwork.class);

    @Resource
    private SynchronousLndAPI api;

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.LIGHTNING_BTC;
    }

    @Override
    public long getBalance(String account) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        PaymentHash paymetHash = new PaymentHash();
        byte[] rHash = Base64.getDecoder().decode(paymentProof);
        try {
            Invoice invoice = api.lookupInvoice(null, rHash);
            return invoice.getSettled();
        } catch (StatusException | ValidationException e) {
            log.warn("Could not verify invoice payment", e);
            return false;
        }
    }

    @Override
    public String getPaymentProofPattern() {
//        return  "^[a-z0-9]{200,250}$";
        return  "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
    }

    public void testConnection() throws SSLException, StatusException, ValidationException {
        SynchronousLndAPI api = new SynchronousLndAPI("localhost",10002,
                new File("/home/alex/.lnd/tls.cert"),
                new File("/home/alex/Repositories/go/dev/bob/data/chain/bitcoin/simnet/admin.macaroon"));
        Invoice invoiceRequest = new Invoice();
        invoiceRequest.setValue(2200);
        AddInvoiceResponse response = api.addInvoice(invoiceRequest);
        System.out.println(response.toJsonAsString(true));
    }

    public static void main(String[] args) throws SSLException, StatusException, ValidationException {
        LightningPaymentNetwork net = new LightningPaymentNetwork();
        net.testConnection();
    }

    public AddInvoiceResponse addInvoice(Long amount, String memo) {
        Invoice invoice = new Invoice();
        invoice.setValue(amount);
        invoice.setMemo(memo);
        try {
            return api.addInvoice(invoice);
        } catch (StatusException | ValidationException e) {
            throw new NetworkCommunicationException(e);
        }
    }
}
