package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.account.HederaAccountAmount;
import com.hedera.sdk.common.*;
import com.hedera.sdk.node.HederaNode;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

@Component
@Lazy
public class HederaClient {

    private HederaAccountID verifyingAccountID;

    private HederaTransactionAndQueryDefaults queryDefaults;

    private HederaAccount verifyingAccount;

    @PostConstruct
    private void init() {
        try {
            this.verifyingAccountID = getVerifyingAccountID();
            this.queryDefaults = newQueryDefaults(verifyingAccountID);
            this.verifyingAccount = getVerifyingAccount(verifyingAccountID, queryDefaults);
        } catch (InvalidKeySpecException | DecoderException e) {
            throw new RuntimeException("Could not initialize querying defaults", e);
        }
    }

    private HederaAccountID getVerifyingAccountID() {
        return new HederaAccountID(0, 0, 1006);
    }

    private HederaTransactionAndQueryDefaults newQueryDefaults(HederaAccountID payingAccountID) throws InvalidKeySpecException, DecoderException {
        String nodeAddress = "testnet.hedera.com";
        int nodePort = 50101;
        HederaAccountID nodeAccountID = new HederaAccountID(0, 0, 3);
        HederaNode node = new HederaNode(nodeAddress, nodePort, nodeAccountID);


        String publicKey = "302a300506032b6570032100e6e4b49e9b39753176071ba9889e750ef3c9cdc21e931d534caa12bead5dfc11";
        String privateKey = "302e020100300506032b657004220420b24de7ad2bfe99ba1037e1e8cc46217e53797f99b4e783257afc69465aa12f4a";
        HederaKeyPair payingKeyPair = new HederaKeyPair(HederaKeyPair.KeyType.ED25519, publicKey, privateKey);


        HederaTransactionAndQueryDefaults queryDefaults = new HederaTransactionAndQueryDefaults();
        queryDefaults.node = node;
        queryDefaults.payingAccountID = payingAccountID;
        queryDefaults.payingKeyPair = payingKeyPair;
        queryDefaults.transactionValidDuration = new HederaDuration(120, 0);
        queryDefaults.generateRecord = false;
        queryDefaults.memo = "Querying";

        return queryDefaults;
    }

    private HederaAccount getVerifyingAccount(HederaAccountID accountId, HederaTransactionAndQueryDefaults queryDefaults) {
        HederaAccount verifyingAccount = new HederaAccount(accountId.shardNum, accountId.realmNum, accountId.accountNum);
        verifyingAccount.txQueryDefaults = queryDefaults;
        verifyingAccount.setNode(verifyingAccount.txQueryDefaults.node);
        return verifyingAccount;
    }

    public boolean verifyPayerPayedPayee(HederaAccount payer, HederaAccount payee, String memo, BigDecimal price) {
        List<HederaTransactionRecord> payerRecords;
        try {
            payerRecords = verifyingAccount.getRecords(payer.shardNum, payer.realmNum, payer.accountNum);
            Optional<HederaTransactionRecord> first = payerRecords.stream()
                    .filter(record -> memoMatches(memo, record))
                    .filter(record -> amountPaid(price, payer, payee, record))
                    .findFirst();
            return first.isPresent();
        } catch (Exception e) {
            throw new RuntimeException("Could no verify transaction", e);
        }
    }

    private boolean amountPaid(BigDecimal price, HederaAccount payer, HederaAccount payee, HederaTransactionRecord record) {
        boolean payerAmountSubtracted = record.transferList.stream().anyMatch(accountAmount -> amountSubtracted(accountAmount, payer, price));
        boolean payeeAmountIncreased  = record.transferList.stream().anyMatch(accountAmount -> amountIncreased(accountAmount, payee, price));
        return payerAmountSubtracted && payeeAmountIncreased;
    }

    private boolean amountSubtracted(HederaAccountAmount accountAmount, HederaAccount payer, BigDecimal price) {
        boolean sameAccount = (accountAmount.accountNum == payer.accountNum);
        boolean atLeastAmount = -accountAmount.amount >= price.longValue();
        return sameAccount && atLeastAmount;
    }

    private boolean amountIncreased(HederaAccountAmount accountAmount, HederaAccount payee, BigDecimal price) {
        boolean sameAccount = (accountAmount.accountNum == payee.accountNum);
        boolean atLeastAmount = accountAmount.amount >= price.longValue();
        return sameAccount && atLeastAmount;
    }

    private boolean memoMatches(String memo, HederaTransactionRecord record) {
        String recordMemo = record.memo;
        return StringUtils.containsIgnoreCase(recordMemo, StringUtils.strip(memo));
    }
}
