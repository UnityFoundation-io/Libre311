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
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, Long> {

    Page<ServiceRequest> findByIdInAndJurisdictionId(List<Long> serviceRequestIds, String jurisdictionId, Pageable pageable);
    List<ServiceRequest> findByIdInAndJurisdictionId(List<Long> serviceRequestIds, String jurisdictionId, Sort sort);
    Optional<ServiceRequest> findByIdAndJurisdictionId(Long serviceRequestId, String jurisdictionId);
    Page<ServiceRequest> findAllByJurisdictionId(String jurisdictionId, Pageable pageable);
    List<ServiceRequest> findAllByJurisdictionId(String jurisdictionId, Sort sort);

    Page<ServiceRequest> findByJurisdictionIdAndServiceIdIn(String jurisdictionId, List<Long> serviceIds, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdIn(String jurisdictionId, List<Long> serviceIds, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, Instant start, Instant end, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, Instant start, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, Instant end, Sort sort);

    Page<ServiceRequest> findByJurisdictionIdAndStatusIn(String jurisdictionId, List<ServiceRequestStatus> status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusIn(String jurisdictionId, List<ServiceRequestStatus> status, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Instant end, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, Instant start, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, Instant end, Sort sort);

    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Instant start, Instant end, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status,  Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Instant start, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, Instant end, Sort sort);

    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBetween(String jurisdictionId, Instant startDate, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedAfter(String jurisdictionId, Instant start, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndDateCreatedBefore(String jurisdictionId, Instant end, Sort sort);

    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant start, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant start, Instant end, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,  Instant start, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant start, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant end, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant end, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant startDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndServiceIdInAndPriorityIn(String jurisdictionId, List<Long> serviceIds, List<ServiceRequestPriority> priority, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityIn(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndStatusInAndPriorityIn(String jurisdictionId, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBetween(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedAfter(String jurisdictionId, List<ServiceRequestPriority> priority, Instant startDate, Sort sort);
    Page<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestPriority> priority, Instant endDate, Pageable pageable);
    List<ServiceRequest> findByJurisdictionIdAndPriorityInAndDateCreatedBefore(String jurisdictionId, List<ServiceRequestPriority> priority, Instant endDate, Sort sort);
    Page<ServiceRequest> findAllByJurisdictionIdAndPriorityIn(String jurisdictionId, List<ServiceRequestPriority> priority, Pageable pageable);
    List<ServiceRequest> findAllByJurisdictionIdAndPriorityIn(String jurisdictionId, List<ServiceRequestPriority> priority, Sort sort);
}
