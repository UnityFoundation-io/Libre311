package app.model.servicerequest;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, String> {
    Page<ServiceRequest> findByIdIn(List<String> serviceRequestIds, Pageable pageable);

    Page<ServiceRequest> findByServiceCode(String serviceCode, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndDateCreatedBetween(String serviceCode, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndDateCreatedAfter(String serviceCode, Instant start, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndDateCreatedBefore(String serviceCode, Instant end, Pageable pageable);

    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedBetween(ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedAfter(ServiceRequestStatus status, Instant start, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedBefore(ServiceRequestStatus status, Instant end, Pageable pageable);

    // todo
    Page<ServiceRequest> findByServiceCodeAndStatus(String serviceCode, ServiceRequestStatus status, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndStatusAndDateCreatedBetween(String serviceCode, ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndStatusAndDateCreatedAfter(String serviceCode, ServiceRequestStatus status,  Instant start, Pageable pageable);
    Page<ServiceRequest> findByServiceCodeAndStatusAndDateCreatedBefore(String serviceCode, ServiceRequestStatus status, Instant end, Pageable pageable);

    Page<ServiceRequest> findByDateCreatedBetween(Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByDateCreatedAfter(Instant start, Pageable pageable);
    Page<ServiceRequest> findByDateCreatedBefore(Instant end, Pageable pageable);
}
