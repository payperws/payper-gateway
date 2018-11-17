package ws.payper.gateway.hedera;


import com.hedera.sdk.common.HederaAccountID;
import com.hedera.sdk.common.HederaDuration;
import com.hedera.sdk.common.HederaKey;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import com.hedera.sdk.cryptography.HederaCryptoKeyPair;
import com.hedera.sdk.node.HederaNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.security.spec.InvalidKeySpecException;

@Configuration
@PropertySource("classpath:hedera-node.properties")
public class HederaNetworkConfiguration {

    @Value("${nodeaddress}")
    private String nodeAddress;

    @Value("${nodeport}")
    private int nodePort;

    @Value("${nodeAccountShard}")
    private long nodeAccountShard;

    @Value("${nodeAccountRealm}")
    private long nodeAccountRealm;

    @Value("${nodeAccountNum}")
    private long nodeAccountNum;

    @Value("${payingAccountShard}")
    private long payingAccountShard;

    @Value("${payingAccountRealm}")
    private long payingAccountRealm;

    @Value("${payingAccountNum}")
    private long payingAccountNum;

    @Value("${pubkey}")
    private String pubKey;

    @Value("${privkey}")
    private String privKey;

    @Bean
    public HederaTransactionAndQueryDefaults txDefaults() {
        HederaTransactionAndQueryDefaults txDefaults = new HederaTransactionAndQueryDefaults();

        HederaAccountID nodeAccountID = new HederaAccountID(nodeAccountShard, nodeAccountRealm, nodeAccountNum);

        HederaNode node = new HederaNode(nodeAddress, nodePort, nodeAccountID);

        HederaAccountID payingAccountID = new HederaAccountID(payingAccountShard, payingAccountRealm, payingAccountNum);

        HederaCryptoKeyPair payingKeyPair = null;
        if (StringUtils.isNoneBlank(pubKey, privKey)) {
            try {
                payingKeyPair = new HederaCryptoKeyPair(HederaKey.KeyType.ED25519, pubKey, privKey);
            } catch (InvalidKeySpecException ex) {
                throw new NetworkConfigurationException("Could not init ED25519 key pair", ex);
            }
        }

        txDefaults.memo = "Demo memo";

        txDefaults.node = node;

        txDefaults.payingAccountID = payingAccountID;

        txDefaults.payingKeyPair = payingKeyPair;

        txDefaults.transactionValidDuration = new HederaDuration(120, 0);

        return txDefaults;
    }
}
