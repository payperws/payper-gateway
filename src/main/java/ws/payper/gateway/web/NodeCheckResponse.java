package ws.payper.gateway.web;

public class NodeCheckResponse {

    private boolean nodeOk;

    private String errorMsg;

    public NodeCheckResponse(boolean nodeOk, String errorMsg) {
        this.nodeOk = nodeOk;
        this.errorMsg = errorMsg;
    }

    public boolean isNodeOk() {
        return nodeOk;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
