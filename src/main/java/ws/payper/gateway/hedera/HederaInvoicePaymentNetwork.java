package ws.payper.gateway.hedera;

import com.hedera.sdk.account.HederaAccount;
import com.hedera.sdk.common.HederaTransactionAndQueryDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.InvoiceRepository;
import ws.payper.gateway.PaymentNetwork;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;

import java.util.Optional;

@Component
public class HederaInvoicePaymentNetwork implements PaymentNetwork {

    @Autowired
    private HederaTransactionAndQueryDefaults queryDefaults;

    @Autowired
    private HederaAccount hederaAccount;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public PaymentOptionType getPaymentOptionType() {
        return PaymentOptionType.HEDERA_HBAR_INVOICE;
    }

    @Override
    public boolean verifyTransaction(String paymentProof, PaymentEndpoint paymentEndpoint, String amount) {
        Optional<Invoice> invoice = invoiceRepository.findByInvoiceId(paymentProof);
        return invoice.map(this::verifyInvoice).orElse(false);
    }

    private boolean verifyInvoice(Invoice invoice) {
        // TODO need account ID of payer to get records and know if payment happened
        return false;
    }

    @Override
    public String getPaymentProofPattern() {
        return "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }

}
