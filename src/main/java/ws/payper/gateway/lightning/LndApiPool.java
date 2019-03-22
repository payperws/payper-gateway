package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.InvoiceRepository;
import ws.payper.gateway.repo.PayableLinkRepository;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LndApiPool {

    private final ConcurrentMap<String, SynchronousLndAPI> lndApis = new ConcurrentHashMap<>();

    @Resource
    private PayableLinkRepository linkRepository;

    @Resource
    private InvoiceRepository invoiceRepository;

    @Resource
    private LightningConnector lightningConnector;

    public SynchronousLndAPI getByRHash(String rHash) {
        Invoice invoice = invoiceRepository.findByInvoiceId(rHash).orElseThrow(() -> new IllegalStateException("Could not find invoice: " + rHash));
        return getByPayableLinkId(invoice.getPayableLinkId());
    }

    public SynchronousLndAPI getByPayableLinkId(String payableLinkId) {
        PayableLink link = linkRepository.findByPayableId(payableLinkId)
                .orElseThrow(() -> new IllegalStateException("Could not find payable link: " + payableLinkId));

        synchronized (lndApis) {
            SynchronousLndAPI lndAPI = lndApis.get(link.getPayableId());
            if (lndAPI == null) {
                lndAPI = connectToNode(payableLinkId);
                lndApis.put(payableLinkId, lndAPI);
            }
            return lndAPI;
        }
    }

    private SynchronousLndAPI connectToNode(String payableLinkId) {
        PayableLink link = linkRepository.findByPayableId(payableLinkId)
                .orElseThrow(() -> new IllegalStateException("Could not find link: " + payableLinkId));

        Map<String, String> args = link.getLinkConfig().getPaymentOptionArgs();

        String host = args.get("pubkeyHost");
        String port = args.get("rpcport");
        String tlsCertBase64 = args.get("tlsCert");
        String macaroonBase64 = args.get("invoiceMacaroon");
        return lightningConnector.connectToNode(host, port, tlsCertBase64, macaroonBase64);
    }
}
