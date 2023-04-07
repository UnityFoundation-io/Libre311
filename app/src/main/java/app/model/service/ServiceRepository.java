package app.model.service;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends PageableRepository<Service, String> {
    Optional<Service> findByServiceCode(String serviceCode);
}
