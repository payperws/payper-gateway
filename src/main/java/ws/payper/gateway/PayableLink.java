package ws.payper.gateway;

import ws.payper.gateway.web.ConfigureLinkController;

public class PayableLink {

    private ConfigureLinkController.LinkConfig linkConfig;

    private String payableId;

    private String payableUrl;

    private String payablePath;

    public PayableLink() {
    }

    public PayableLink(ConfigureLinkController.LinkConfig linkConfig, String payableId, String payableUrl, String payablePath) {
        this.linkConfig = linkConfig;
        this.payableId = payableId;
        this.payableUrl = payableUrl;
        this.payablePath = payablePath;
    }

    public ConfigureLinkController.LinkConfig getLinkConfig() {
        return linkConfig;
    }

    public String getPayableId() {
        return payableId;
    }

    public String getPayableUrl() {
        return payableUrl;
    }

    public String getPayablePath() {
        return payablePath;
    }

    public void setLinkConfig(ConfigureLinkController.LinkConfig linkConfig) {
        this.linkConfig = linkConfig;
    }

    public void setPayableId(String payableId) {
        this.payableId = payableId;
    }

    public void setPayableUrl(String payableUrl) {
        this.payableUrl = payableUrl;
    }

    public void setPayablePath(String payablePath) {
        this.payablePath = payablePath;
    }
}
