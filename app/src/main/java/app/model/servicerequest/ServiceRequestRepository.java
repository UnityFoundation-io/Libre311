package app.model.servicerequest;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, String> {
}
