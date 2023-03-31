package app.model.service;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface ServiceRepository extends PageableRepository<Service, String> {
    Page<Service> findByServiceNameContains(String name, Pageable pageable);
}
