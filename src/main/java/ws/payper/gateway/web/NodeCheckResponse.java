package ws.payper.gateway.web;

public class NodeCheckResponse {

    private boolean nodeOk;

    public NodeCheckResponse() {
    }

    public NodeCheckResponse(boolean nodeOk) {
        this.nodeOk = nodeOk;
    }

    public boolean isNodeOk() {
        return nodeOk;
    }
}
