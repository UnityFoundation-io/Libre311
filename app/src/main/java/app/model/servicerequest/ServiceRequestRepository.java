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
import io.micronaut.data.jpa.repository.JpaSpecificationExecutor;
import io.micronaut.data.jpa.repository.criteria.Specification;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {

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

    @Transactional
    default List<ServiceRequest> findAllBy(String jurisdictionId, List<Long> serviceCodes,
                                           List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,
                                           Instant startDate, Instant endDate, Sort sort) {

        Specification<ServiceRequest> specification;
        specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("jurisdiction_id"), jurisdictionId);

        if (serviceCodes != null && !serviceCodes.isEmpty()) {
            specification.and(Specifications.serviceCodeIn(serviceCodes));
        }

//        if (status != null && !status.isEmpty()) {
//            specification.and(Specifications.statusIn(status));
//        }
//
//        if (priority != null && !priority.isEmpty()) {
//            specification.and(Specifications.priorityIn(priority));
//        }


        if (startDate != null && endDate != null) {
            specification.and(Specifications.createdDateBetween(startDate, endDate));
        } else if (startDate != null && endDate == null) {
            specification.and(Specifications.createdDateAfter(startDate));
        } else if (startDate == null && endDate != null) {
            specification.and(Specifications.createdDateBefore(endDate));
        }

        return findAll(specification, sort);
    }

    class Specifications {

        // serviceCode
        public static Specification<ServiceRequest> serviceCodeIn(List<Long> serviceCodes) {
            return (root, query, criteriaBuilder)
                    -> root.get("service_id").in(serviceCodes);
        }

//        // status
//        public static Specification<ServiceRequest> statusIn(List<ServiceRequestStatus> serviceRequestStatuses) {
//            return (root, query, criteriaBuilder)
//                    -> root.get("service_id").in(serviceCodes);
//        }
//
//        // priority
//        public static Specification<ServiceRequest> priorityIn(List<ServiceRequestPriority> serviceRequestPriorities) {
//            return (root, query, criteriaBuilder)
//                    -> root.get("service_id").in(serviceCodes);
//        }

        // dateCreated
        public static Specification<ServiceRequest> createdDateBetween(Instant startDate, Instant endDate) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.between(root.get("date_created"), startDate, endDate);
        }

        public static Specification<ServiceRequest> createdDateAfter(Instant instant) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.greaterThanOrEqualTo(root.get("date_created"), instant);
        }

        public static Specification<ServiceRequest> createdDateBefore(Instant instant) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.lessThanOrEqualTo(root.get("date_created"), instant);
        }
    }

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
