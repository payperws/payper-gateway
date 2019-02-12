package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

@Component
public class HederaInvoicePaymentNetwork implements PaymentNetwork {

    @Autowired
    private HederaTransactionAndQueryDefaults queryDefaults;

    @Autowired
    private HederaAccount hederaAccount;

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR_INVOICE;
    }

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
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        return false;
    }

    @Override
    public String getPaymentProofPattern() {
        return "^[0-9]{3,4}\\|[0-9]{4,10}";
    }

}
