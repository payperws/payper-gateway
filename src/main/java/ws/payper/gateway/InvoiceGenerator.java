package ws.payper.gateway;

import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.model.Invoice;
import ws.payper.gateway.web.InvoiceRequest;

public interface InvoiceGenerator {

    Invoice newInvoice(PayableLink link);

    PaymentOptionType getPaymentOptionType();
}
