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

    Optional<ServiceRequest> findByServiceServiceNameIlikeAndJurisdictionId(String serviceName, String jurisdictionId);
    Page<ServiceRequest> findByIdInAndJurisdictionId(List<Long> serviceRequestIds, String jurisdictionId, Pageable pageable);
    Optional<ServiceRequest> findByIdAndJurisdictionId(Long serviceRequestId, String jurisdictionId);
    Page<ServiceRequest> findAllByJurisdictionId(String jurisdictionId, Pageable pageable);
    List<ServiceRequest> findAllByJurisdictionId(String jurisdictionId);

    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeIn(String jurisdictionId, List<String> serviceCodes, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCode(String jurisdictionId, String serviceCode);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedBetween(String jurisdictionId, List<String> serviceCodes, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(String jurisdictionId, String serviceCode, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedAfter(String jurisdictionId, List<String> serviceCodes, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(String jurisdictionId, String serviceCode, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedBefore(String jurisdictionId, List<String> serviceCodes, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(String jurisdictionId, String serviceCode, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndStatusIn(String jurisdictionId, List<ServiceRequestStatus> status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusIn(String jurisdictionId, List<ServiceRequestStatus> status);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusIn(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusIn(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedBetween(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedBetween(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedAfter(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status,  Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedAfter(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status,  Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedBefore(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedBefore(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant startDate, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end);

    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityIn(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityIn(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant start, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,  Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,  Instant start);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, String serviceCode, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant end);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedBetween(String jurisdictionId, String serviceCode, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedAfter(String jurisdictionId, String serviceCode, List<ServiceRequestPriority> priority, Instant startDate);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedBefore(String jurisdictionId, String serviceCode, List<ServiceRequestPriority> priority, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeInAndPriorityIn(String jurisdictionId, List<String> serviceCodes, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceServiceCodeAndPriorityIn(String jurisdictionId, String serviceCode, List<ServiceRequestPriority> priority);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityIn(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityIn(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestPriority> priority, Instant endDate);
    Page<ServiceRequest> findAllByJurisdictionIdAndPriorityIn(String jurisdictionId, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findAllByJurisdictionIdAndPriorityIn(String jurisdictionId, List<ServiceRequestPriority> priority);
}
