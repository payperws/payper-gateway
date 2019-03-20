package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.MacaroonContext;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.springframework.stereotype.Component;
import ws.payper.gateway.hedera.NetworkCommunicationException;
import ws.payper.gateway.repo.PayableLinkRepository;
import ws.payper.gateway.web.ConfigureLinkController;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Component
public class LndApiPool {

    @Resource
    private PayableLinkRepository linkRepository;

    public SynchronousLndAPI getByRHash(byte[] rHash) {
        return null;
    }

    public SynchronousLndAPI getByPayableLinkId(String payableLinkId) {
        return null;
    }

    private SynchronousLndAPI connectToNode(String payableLinkId) {
        ConfigureLinkController.PayableLink link = linkRepository.find(payableLinkId)
                .orElseThrow(() -> new IllegalStateException("Could not find link: " + payableLinkId));

        Map<String, String> args = link.getLinkConfig().getPaymentOptionArgs();

        String host = args.get("pubkeyHost");
        int port = Integer.parseInt(args.get("rpcport"));
        String tlsCertBase64 = args.get("tlsCert");
        File tlsCertFile = tlsCertFileFromBase64(tlsCertBase64);
        String macaroonBase64 = args.get("invoiceMacaroon");
        File macaroonFile = macaroonFileFromBase64(macaroonBase64);
        try {
            return new SynchronousLndAPI(host, port, tlsCertFile, macaroonFile);
        } catch (SSLException | ClientSideException e) {
            throw new NetworkCommunicationException("Could not connect to network for link: " + payableLinkId, e);
        }
    }

    private File tlsCertFileFromBase64(String tlsCertBase64) {
        byte[] bytes = Base64.getDecoder().decode(tlsCertBase64);
        try {
            File tempFile = File.createTempFile("tls", ".cert");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bytes);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Could not write to temp cert file.", e);
        }
    }

    private File macaroonFileFromBase64(String macaroonBase64) {
        byte[] bytes = Base64.getDecoder().decode(macaroonBase64);
        try {
            File tempFile = File.createTempFile("invoice", ".macaroon");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bytes);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Could not write to temp macaroon file.", e);
        }
    }

    private MacaroonContext macaroonContextFromBase64(String macaroonBase64) {
        byte[] bytes = Base64.getDecoder().decode(macaroonBase64);
        return new ByteArrayMacaroonContext(bytes);
    }
}
