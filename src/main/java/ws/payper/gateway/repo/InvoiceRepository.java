package ws.payper.gateway.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ws.payper.gateway.model.Invoice;

import java.util.Optional;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    @Override
    <S extends Invoice> S save(S s);

    Optional<Invoice> findByInvoiceId(String invoiceId);
}
