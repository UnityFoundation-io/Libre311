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

    Page<ServiceRequest> findByServiceServiceCode(String serviceCode, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedBetween(String serviceCode, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedAfter(String serviceCode, Instant start, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedBefore(String serviceCode, Instant end, Pageable pageable);

    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedBetween(ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedAfter(ServiceRequestStatus status, Instant start, Pageable pageable);
    Page<ServiceRequest> findByStatusAndDateCreatedBefore(ServiceRequestStatus status, Instant end, Pageable pageable);

    Page<ServiceRequest> findByServiceServiceCodeAndStatus(String serviceCode, ServiceRequestStatus status, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBetween(String serviceCode, ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedAfter(String serviceCode, ServiceRequestStatus status,  Instant start, Pageable pageable);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBefore(String serviceCode, ServiceRequestStatus status, Instant end, Pageable pageable);

    Page<ServiceRequest> findByDateCreatedBetween(Instant start, Instant end, Pageable pageable);
    Page<ServiceRequest> findByDateCreatedAfter(Instant start, Pageable pageable);
    Page<ServiceRequest> findByDateCreatedBefore(Instant end, Pageable pageable);
}
