package app.model.service.group;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import java.util.Optional;

@Repository
public interface ServiceGroupRepository extends PageableRepository<ServiceGroup, Long> {

  Optional<ServiceGroup> findByName(String name);
}
