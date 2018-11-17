package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;

@Component
public class HederaPaymentNetwork implements PaymentNetwork {

    @Autowired
    private HederaTransactionAndQueryDefaults queryDefaults;

    @Override
    public long getBalance(String accountId) {
        long accountNum;
        try {
            accountNum = Long.parseLong(accountId);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Could not parse accountId to long");
        }

        HederaAccount account = new HederaAccount();

        account.txQueryDefaults = queryDefaults;

        account.accountNum = accountNum;

        try {
            return account.getBalance();
        } catch (Exception e) {
            throw new NetworkCommunicationException("Could not retrieve balance for account " + accountId, e);
        }
    }

    @Override
    public boolean verifyTransaction(String account, String amount) {
        return false;
    }
}
