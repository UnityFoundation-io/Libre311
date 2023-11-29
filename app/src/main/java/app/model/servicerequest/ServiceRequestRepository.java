// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

    // jurisdiction
    Optional<ServiceRequest> findByIdAndJurisdictionId(Long serviceRequestId, String jurisdictionId);
    Page<ServiceRequest> findAllByJurisdictionId(String jurisdictionId, Pageable pageable);
    List<ServiceRequest> findAllByJurisdictionId(String jurisdictionId);

    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCode(String jurisdictionId, String serviceCode, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCode(String jurisdictionId, String serviceCode);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(String jurisdictionId, String serviceCode, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(String jurisdictionId, String serviceCode, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(String jurisdictionId, String serviceCode, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(String jurisdictionId, String serviceCode, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(String jurisdictionId, String serviceCode, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(String jurisdictionId, String serviceCode, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndStatus(String jurisdictionId, ServiceRequestStatus status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatus(String jurisdictionId, ServiceRequestStatus status);
    Page<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedBetween(String jurisdictionId, ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedBetween(String jurisdictionId, ServiceRequestStatus status, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedAfter(String jurisdictionId, ServiceRequestStatus status, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedAfter(String jurisdictionId, ServiceRequestStatus status, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedBefore(String jurisdictionId, ServiceRequestStatus status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusAndDateCreatedBefore(String jurisdictionId, ServiceRequestStatus status, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatus(String jurisdictionId, String serviceCode, ServiceRequestStatus status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatus(String jurisdictionId, String serviceCode, ServiceRequestStatus status);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBetween(String jurisdictionId, String serviceCode, ServiceRequestStatus status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBetween(String jurisdictionId, String serviceCode, ServiceRequestStatus status, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedAfter(String jurisdictionId, String serviceCode, ServiceRequestStatus status,  Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedAfter(String jurisdictionId, String serviceCode, ServiceRequestStatus status,  Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBefore(String jurisdictionId, String serviceCode, ServiceRequestStatus status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBefore(String jurisdictionId, String serviceCode, ServiceRequestStatus status, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant startDate, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end);
}
