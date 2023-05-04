package app.model.servicerequest;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, Long> {
    Page<ServiceRequest> findByIdIn(List<Long> serviceRequestIds, Pageable pageable);

    Page<ServiceRequest> findByServiceServiceCode(String serviceCode, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCode(String serviceCode);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedBetween(String serviceCode, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndDateCreatedBetween(String serviceCode, Instant start, Instant end);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedAfter(String serviceCode, Instant start, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndDateCreatedAfter(String serviceCode, Instant start);
    Page<ServiceRequest> findByServiceServiceCodeAndDateCreatedBefore(String serviceCode, Instant end, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndDateCreatedBefore(String serviceCode, Instant end);

    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
    List<ServiceRequest> findByStatus(ServiceRequestStatus status);
    Page<ServiceRequest> findByStatusAndDateCreatedBetween(ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByStatusAndDateCreatedBetween(ServiceRequestStatus status, Instant start, Instant end);
    Page<ServiceRequest> findByStatusAndDateCreatedAfter(ServiceRequestStatus status, Instant start, Pageable pageable);
    List<ServiceRequest> findByStatusAndDateCreatedAfter(ServiceRequestStatus status, Instant start);
    Page<ServiceRequest> findByStatusAndDateCreatedBefore(ServiceRequestStatus status, Instant end, Pageable pageable);
    List<ServiceRequest> findByStatusAndDateCreatedBefore(ServiceRequestStatus status, Instant end);

    Page<ServiceRequest> findByServiceServiceCodeAndStatus(String serviceCode, ServiceRequestStatus status, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndStatus(String serviceCode, ServiceRequestStatus status);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBetween(String serviceCode, ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBetween(String serviceCode, ServiceRequestStatus status, Instant start, Instant end);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedAfter(String serviceCode, ServiceRequestStatus status,  Instant start, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedAfter(String serviceCode, ServiceRequestStatus status,  Instant start);
    Page<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBefore(String serviceCode, ServiceRequestStatus status, Instant end, Pageable pageable);
    List<ServiceRequest> findByServiceServiceCodeAndStatusAndDateCreatedBefore(String serviceCode, ServiceRequestStatus status, Instant end);

    Page<ServiceRequest> findByDateCreatedBetween(Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByDateCreatedBetween(Instant startDate, Instant endDate);
    Page<ServiceRequest> findByDateCreatedAfter(Instant start, Pageable pageable);
    List<ServiceRequest> findByDateCreatedAfter(Instant start);
    Page<ServiceRequest> findByDateCreatedBefore(Instant end, Pageable pageable);
    List<ServiceRequest> findByDateCreatedBefore(Instant end);

    Optional<ServiceRequest> findByServiceServiceNameIlike(String serviceName);
}
