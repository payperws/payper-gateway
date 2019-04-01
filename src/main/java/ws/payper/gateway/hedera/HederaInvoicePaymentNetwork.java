package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.HederaBuyer;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.HederaBuyersRepository;
import ws.payper.gateway.repo.InvoiceRepository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class HederaInvoicePaymentNetwork implements PaymentNetwork {

    private static final String ACCOUNT_ID_PATTERN = "^[0-9]{1,5}\\.[0-9]{1,5}\\.[0-9]{1,19}";

    private List<HederaBuyer> buyers = new ArrayList<>();

    private final List<HederaBuyer> hardcodedBuyers = List.of(
            HederaBuyer.of("0.0.1011")
    );

    @Autowired
    private HederaBuyersRepository buyersRepo;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private HederaClient hederaClient;

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR_INVOICE;
    }

    @PostConstruct
    private void init() {
        this.buyers.addAll(hardcodedBuyers);
        this.buyers.addAll(buyersRepo.findAll());
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        Optional<Invoice> invoice = invoiceRepository.findByInvoiceId(paymentProof);
        return invoice.map(this::verifyInvoice).orElse(false);
    }

    private boolean verifyInvoice(Invoice invoice) {
        return buyers.stream().anyMatch(b -> paidInvoice(b, invoice));
    }

    @Override
    public String getPaymentProofPattern() {
        return "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }

    private boolean paidInvoice(HederaBuyer buyer, Invoice invoice) {
        HederaAccount payer = getHederaAccount(buyer.getAccountId());
        HederaAccount payee = getHederaAccount(getAccountFromInvoice(invoice));
        String memo = invoice.getInvoiceId();
        return hederaClient.verifyPayerPayedPayee(payer, payee, memo, invoice.getPayableLink().getLinkConfig().getPrice());
    }

    private String getAccountFromInvoice(Invoice invoice) {
        String account = invoice.getPayableLink().getLinkConfig().getPaymentOptionArgs().get("account");
        if (StringUtils.isBlank(account)) {
            throw new IllegalArgumentException("could not find account in invoice. InvoiceID: " + invoice.getInvoiceId());
        }
        return account;
    }

    private HederaAccount getHederaAccount(String accountId) {
        if ((StringUtils.isBlank(accountId)) || (!accountId.matches(ACCOUNT_ID_PATTERN))) {
            throw new IllegalArgumentException("Invalid account ID: [" + accountId + "]");
        }
        String[] splits = accountId.split("\\.");
        long shard = Integer.parseInt(splits[0]);
        long realm = Integer.parseInt(splits[1]);
        long account = Integer.parseInt(splits[2]);
        return new HederaAccount(shard, realm, account);
    }
}
