package ws.payper.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ws.payper.gateway.config.PaymentOptionType;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PaymentRequiredController {

    private List<InvoiceGenerator> invoiceGeneratorList;

    private Map<PaymentOptionType, InvoiceGenerator> invoiceGenerators;

    private final String DEFAULT_VIEW = "payment-required";

    private Map<PaymentOptionType, String> views = Map.of(
            PaymentOptionType.HEDERA_HBAR, DEFAULT_VIEW,
            PaymentOptionType.HEDERA_HBAR_INVOICE, "payment-required-hedera-invoice",
            PaymentOptionType.LIGHTNING_BTC, "payment-required-lightning-btc"
    );

    @Autowired
    public void setInvoiceGeneratorList(List<InvoiceGenerator> invoiceGeneratorList) {
        this.invoiceGenerators = invoiceGeneratorList.stream().collect(Collectors.toMap(InvoiceGenerator::getPaymentOptionType, Function.identity()));
    }

    @RequestMapping("/pypr/payment-required")
    @ResponseStatus(code = HttpStatus.PAYMENT_REQUIRED)
    public String paymentRequired(
                                  @RequestParam(value = "title") String title,
                                  @RequestParam(value = "sourceurl") String sourceUrl,
                                  @RequestParam(value = "option") PaymentOptionType paymentOptionType,
                                  @RequestParam(value = "amount") String amount,
                                  @RequestParam(value = "account", required = false) String account,
                                  Model model) {

        InvoiceGenerator invoiceGenerator = invoiceGenerators.get(paymentOptionType);

        if (invoiceGenerator == null) {
            throw new IllegalArgumentException("Payment option not supported: " + paymentOptionType.name());
        }

        URL url;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        InvoiceRequest invoiceRequest = new InvoiceRequest(title, url, paymentOptionType, amount);
        invoiceRequest.setAccount(account);

        Invoice invoice = invoiceGenerator.newInvoice(invoiceRequest);

        model.addAllAttributes(invoice.allParameters());

        return views.getOrDefault(paymentOptionType, DEFAULT_VIEW);
    }
}
