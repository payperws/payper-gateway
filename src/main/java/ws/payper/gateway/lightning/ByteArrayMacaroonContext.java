package ws.payper.gateway.lightning;

import org.lightningj.lnd.wrapper.MacaroonContext;

import javax.xml.bind.DatatypeConverter;

public class ByteArrayMacaroonContext implements MacaroonContext {

    private String currentMacaroonData;

    public ByteArrayMacaroonContext(byte[] data) {
        this.currentMacaroonData = DatatypeConverter.printHexBinary(data);
    }

    @Override
    public String getCurrentMacaroonAsHex() {
        return currentMacaroonData;
    }
}
