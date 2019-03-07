package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.*;
import com.hedera.sdk.query.HederaQueryHeader;
import com.hedera.sdk.transaction.HederaTransaction;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

@Component
public class HederaPaymentNetwork implements PaymentNetwork {

    @Autowired
    private HederaTransactionAndQueryDefaults queryDefaults;

    @Autowired
    private HederaAccount hederaAccount;

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR;
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        HederaTransactionID hederaTransactionID = parseTransactionId(paymentProof);
        HederaTransactionReceipt receipt;
        try {
            receipt = Utilities.getReceipt(hederaTransactionID, queryDefaults.node, 1, 0, 0);
        } catch (InterruptedException ex) {
            throw new NetworkCommunicationException("Could not verify transaction ID", ex);
        }

        if (receipt.transactionStatus == ResponseCodeEnum.SUCCESS) {
            HederaTransactionRecord txRecord;
            try {
                txRecord = getHederaTransactionRecord(hederaTransactionID, 10, queryDefaults);
            } catch (Exception ex) {
                throw new NetworkCommunicationException("Could not retrieve transaction record", ex);
            }
            String account =  ((HederaHbarPaymentEndpoint) paymentEndpoint).getAccount();
            return txRecord.transferList.stream()
                    .anyMatch(h -> matchTransfer(h, account, amount));
        }

        return false;
    }

    private HederaTransactionRecord getHederaTransactionRecord(HederaTransactionID transactionID, int queryFee, HederaTransactionAndQueryDefaults txQueryDefaults) throws Exception {
        HederaTransactionRecord record = new HederaTransactionRecord();
        HederaTransaction transaction = new HederaTransaction();
        HederaTransaction payment = new HederaTransaction(txQueryDefaults, queryFee);

        transaction.setNode(txQueryDefaults.node);

        if (transaction.getRecord(payment, transactionID, HederaQueryHeader.QueryResponseType.ANSWER_ONLY)) {
            record.consensusTimeStamp = transaction.transactionRecord().consensusTimeStamp;
            record.contractCallResult = transaction.transactionRecord().contractCallResult;
            record.contractCreateResult = transaction.transactionRecord().contractCreateResult;
            record.memo = transaction.transactionRecord().memo;
            record.transactionFee = transaction.transactionRecord().transactionFee;
            record.transactionHash = transaction.transactionRecord().transactionHash;
            record.transactionId = transaction.transactionRecord().transactionId;
            record.transactionReceipt = transaction.transactionRecord().transactionReceipt;
            record.transferList = transaction.transactionRecord().transferList;
        }
        return record;
    }

    private boolean matchTransfer(HederaAccountAmount h, String account, String amount) {
        long verifiedAccount = Long.parseLong(account);
        long verifiedAmount = Long.parseLong(amount);
        return h.accountNum == verifiedAccount && h.amount == verifiedAmount;
    }

    private HederaTransactionID parseTransactionId(String transactionId) {
        String[] slices = transactionId.split("\\|");

        long seconds = Long.parseLong(slices[0]);
        int nanos = Integer.parseInt(slices[1]);
        long parsedAccountId = Long.parseLong(slices[2]);

        // TODO get shard and realm numbers from transaction ID string

        HederaAccountID accountID = new HederaAccountID(queryDefaults.payingAccountID.shardNum, queryDefaults.payingAccountID.realmNum, parsedAccountId);
        HederaTimeStamp timestamp = new HederaTimeStamp(seconds, nanos);

        return new HederaTransactionID(accountID, timestamp);
    }

    @Override
    public String getPaymentProofPattern() {
        return "^[0-9]{8,10}\\|[0-9]{1,9}\\|[0-9]{4,10}";
    }

}
