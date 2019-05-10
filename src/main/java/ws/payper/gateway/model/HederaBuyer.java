package ws.payper.gateway.model;

public class HederaBuyer {

    private String accountId;

    public HederaBuyer() {
    }

    public HederaBuyer(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public static HederaBuyer of(String accountId) {
        return new HederaBuyer(accountId);
    }
}
