package ws.payper.gateway.web;

public class NodeCheckRequest {
    private String pubkeyHost;
    private String rpcport;
    private String invoiceMacaroon;
    private String tlsCert;

    public String getPubkeyHost() {
        return pubkeyHost;
    }

    public void setPubkeyHost(String pubkeyHost) {
        this.pubkeyHost = pubkeyHost;
    }

    public String getRpcport() {
        return rpcport;
    }

    public void setRpcport(String rpcport) {
        this.rpcport = rpcport;
    }

    public String getInvoiceMacaroon() {
        return invoiceMacaroon;
    }

    public void setInvoiceMacaroon(String invoiceMacaroon) {
        this.invoiceMacaroon = invoiceMacaroon;
    }

    public String getTlsCert() {
        return tlsCert;
    }

    public void setTlsCert(String tlsCert) {
        this.tlsCert = tlsCert;
    }
}
