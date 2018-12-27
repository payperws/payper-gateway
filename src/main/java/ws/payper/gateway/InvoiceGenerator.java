package ws.payper.gateway;

import ws.payper.gateway.config.PaymentOptionType;

public interface InvoiceGenerator {

    Invoice newInvoice(InvoiceRequest request);

    PaymentOptionType getPaymentOptionType();
}
