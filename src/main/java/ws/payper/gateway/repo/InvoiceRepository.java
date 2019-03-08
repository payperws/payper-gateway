package ws.payper.gateway.repo;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import ws.payper.gateway.model.Invoice;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InvoiceRepository implements Repository<Invoice, Long> {

    private ConcurrentMap<String, Invoice> invoiceIds = new ConcurrentHashMap<>();

    public synchronized Invoice save(Invoice invoice) {
        String invoiceId = invoice.getPaymentOptionParameters().get("invoice_id");
        invoiceIds.putIfAbsent(invoiceId, invoice);
        return invoice;
    }

    public synchronized Optional<Invoice> find(String invoiceId) {
        return Optional.ofNullable(invoiceIds.get(invoiceId));
    }
}
