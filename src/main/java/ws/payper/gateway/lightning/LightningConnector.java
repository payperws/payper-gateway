package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ws.payper.gateway.hedera.NetworkCommunicationException;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Base64;

@Component
public class LightningConnector {

    private final Logger log = LoggerFactory.getLogger(LightningConnector.class);

    public boolean checkNode(String host, String port, String tlsCertBase64, String macaroonBase64) {
        try {
            checkNodeInvoiceAccess(host, port, tlsCertBase64, macaroonBase64);
            return true;
        } catch (NetworkCommunicationException e) {
            log.info("Node check invoice access error: {}", e.getMessage());
            return false;
        }
    }

    public boolean checkNodeInvoiceAccess(String host, String port, String tlsCertBase64, String macaroonBase64) {
        SynchronousLndAPI api = connectToNode(host, port, tlsCertBase64, macaroonBase64);
        try {
            api.listInvoices(true, 0L, 1L, false);
            api.close();
            return true;
        } catch (StatusException | ValidationException e) {
            throw new NetworkCommunicationException(e);
        }
    }

    public SynchronousLndAPI connectToNode(String host, String port, String tlsCertBase64, String macaroonBase64) {
        int intport = Integer.parseInt(port);
        File tlsCertFile = tlsCertFileFromBase64(tlsCertBase64);
        File macaroonFile = macaroonFileFromBase64(macaroonBase64);
        try {
            return new SynchronousLndAPI(host, intport, tlsCertFile, macaroonFile);
        } catch (SSLException | ClientSideException e) {
            String message = MessageFormat.format("Could not connect to Lightning Node: {0}:{1}", host, port);
            throw new NetworkCommunicationException(message, e);
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
