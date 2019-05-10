package ws.payper.gateway.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.PaymentRequestVerifier;
import ws.payper.gateway.config.PaymentEndpoint;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.dummy.DummyCoinPaymentEndpoint;
import ws.payper.gateway.hedera.HederaHbarInvoicePaymentEndpoint;
import ws.payper.gateway.hedera.HederaHbarPaymentEndpoint;
import ws.payper.gateway.lightning.LightningBtcPaymentEndpoint;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.InvoiceRepository;

@Controller
public class VerifyPaymentController {

    @Autowired
    private PaymentRequestVerifier verifier;

    @Autowired
    private InvoiceRepository invoiceRepo;

    @RequestMapping("/verify")
    public @ResponseBody
    VerificationResponse verify(@RequestParam("x_payment_receipt") String invoiceId) {
        Invoice invoice = invoiceRepo.findByInvoiceId(invoiceId).orElseThrow(() -> new IllegalArgumentException("Could not find invoice: " + invoiceId));
        PayableLink link = invoice.getPayableLink();
        PaymentEndpoint endpoint = getEndpoint(link);
        boolean verified = verifier.verifyReceipt(endpoint, invoiceId, link.getLinkConfig().getPrice().toString());
        return new VerificationResponse(verified);
    }

    // TODO: duplicate - do not use PaymentEndpoint!
    private PaymentEndpoint getEndpoint(PayableLink link) {
        PaymentOptionType type = link.getLinkConfig().getPaymentOptionType();
        PaymentEndpoint paymentEndpoint;
        if (PaymentOptionType.DUMMY_COIN.equals(type)) {
            paymentEndpoint = new DummyCoinPaymentEndpoint();
        } else if (PaymentOptionType.LIGHTNING_BTC.equals(type)) {
            paymentEndpoint = new LightningBtcPaymentEndpoint();
        } else if (PaymentOptionType.HEDERA_HBAR_INVOICE.equals(type)) {
            paymentEndpoint = new HederaHbarInvoicePaymentEndpoint();
        } else {
            String account = link.getLinkConfig().getPaymentOptionArgs().get("account");
            paymentEndpoint = new HederaHbarPaymentEndpoint(account);
        }
        return paymentEndpoint;
    }

    public static class VerificationResponse {

        private final boolean verified;

        public VerificationResponse(boolean b) {
            verified = b;
        }

        public boolean isVerified() {
            return verified;
        }
    }
}
