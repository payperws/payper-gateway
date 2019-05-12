package ws.payper.gateway.web;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ws.payper.gateway.InvoiceGenerator;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.repo.InvoiceRepository;
import ws.payper.gateway.repo.PayableLinkRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PaymentRequiredController {

    private List<InvoiceGenerator> invoiceGeneratorList;

    private Map<PaymentOptionType, InvoiceGenerator> invoiceGenerators;

    @Autowired
    private PayableLinkRepository linkRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    public void setInvoiceGeneratorList(List<InvoiceGenerator> invoiceGeneratorList) {
        this.invoiceGenerators = invoiceGeneratorList.stream().collect(Collectors.toMap(InvoiceGenerator::getPaymentOptionType, Function.identity()));
    }

    @RequestMapping("/pay-details")
    @ResponseBody
    public Invoice paymentDetails(@RequestParam(value = "link-id") String linkId,
                                  @RequestParam(value = "invoice-id", required = false) String invoiceId) {

        PayableLink link = linkRepository.findByPayableId(linkId)
                .orElseThrow(() -> new LinkNotFoundException("linkId: " + linkId));

        Invoice invoice;
        if (StringUtils.isBlank(invoiceId)) {
            invoice = newInvoice(link);
        } else {
            invoice = invoiceRepository.findByInvoiceId(invoiceId).orElseThrow(
                    () -> new IllegalArgumentException("Could not find invoice in repo. ID: " + invoiceId));
        }
        return invoice;
    }

    private Invoice newInvoice(PayableLink link) {
        PaymentOptionType paymentOptionType = link.getLinkConfig().getPaymentOptionType();
        InvoiceGenerator invoiceGenerator = invoiceGenerators.get(paymentOptionType);

        if (invoiceGenerator == null) {
            throw new IllegalArgumentException("Payment option not supported: " + paymentOptionType.name());
        }

        Invoice invoice = invoiceGenerator.newInvoice(link);
        invoice = invoiceRepository.save(invoice);
        return invoice;
    }
}
