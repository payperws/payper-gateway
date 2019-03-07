package ws.payper.gateway.repo;

import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import ws.payper.gateway.web.ConfigureLinkController;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class PayableLinkRepository implements Repository<ConfigureLinkController.PayableLink, Long> {

    private ConcurrentMap<String, ConfigureLinkController.PayableLink> payableIds = new ConcurrentHashMap<>();

    public synchronized ConfigureLinkController.PayableLink save(ConfigureLinkController.PayableLink link) {
        String id = link.getPayableId();

        if (payableIds.containsKey(id)) {
            throw new IllegalArgumentException("Payable ID is already registered: " + id);
        } else {
            payableIds.put(id, link);
        }

        return link;
    }

    public synchronized Optional<ConfigureLinkController.PayableLink> find(String payableId) {
        return Optional.ofNullable(payableIds.get(payableId));
    }
}
