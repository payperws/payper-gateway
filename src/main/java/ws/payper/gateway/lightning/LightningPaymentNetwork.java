package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.AddInvoiceResponse;
import org.lightningj.lnd.wrapper.message.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.hedera.NetworkCommunicationException;

import javax.annotation.Resource;
import java.util.Base64;

@Component
public class LightningPaymentNetwork implements PaymentNetwork {

    private final Logger log = LoggerFactory.getLogger(LightningPaymentNetwork.class);

    @Resource
    private LndApiPool lndApiPool;

    public LightningPaymentNetwork() {
    }

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.LIGHTNING_BTC;
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        byte[] rHash = Base64.getDecoder().decode(paymentProof);
        try {
            SynchronousLndAPI api = lndApiPool.getByRHash(paymentProof);
            Invoice invoice = api.lookupInvoice(null, rHash);
            return invoice.getSettled();
        } catch (StatusException | ValidationException e) {
            log.warn("Could not verify invoice payment", e);
            return false;
        }
    }

    @Override
    public String getPaymentProofPattern() {
        return "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
    }

    public AddInvoiceResponse addInvoice(String payableLinkId, Long amount, String memo) {
        Invoice invoice = new Invoice();
        invoice.setValue(amount);
        invoice.setMemo(memo);
        try {
            SynchronousLndAPI api = lndApiPool.getByPayableLinkId(payableLinkId);
            return api.addInvoice(invoice);
        } catch (StatusException | ValidationException e) {
            throw new NetworkCommunicationException(e);
        }
    }
}
