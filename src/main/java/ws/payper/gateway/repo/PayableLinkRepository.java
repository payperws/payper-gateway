package ws.payper.gateway.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import ws.payper.gateway.PayableLink;

import java.util.Optional;

public interface PayableLinkRepository extends MongoRepository<PayableLink, String> {

    @Override
    <S extends PayableLink> S save(S s);

    Optional<PayableLink> findByPayableId(String s);
}
