package ws.payper.gateway.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ws.payper.gateway.model.HederaBuyer;

import java.util.List;

public interface HederaBuyersRepository extends MongoRepository<HederaBuyer, String> {

    @Override
    <S extends HederaBuyer> S save(S s);

    @Override
    List<HederaBuyer> findAll();
}
