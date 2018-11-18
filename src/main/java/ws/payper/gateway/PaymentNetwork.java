package ws.payper.gateway;

public interface PaymentNetwork {

    long getBalance(String account);

    boolean verifyTransaction(String transactionId, String account, String amount);

}
