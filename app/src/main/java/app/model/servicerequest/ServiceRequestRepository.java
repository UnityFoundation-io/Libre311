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

    @Transactional
    default Page<ServiceRequest> findAllBy(String jurisdictionId, List<Long> serviceCodes,
                                           List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,
                                           Instant startDate, Instant endDate, Pageable pageable) {

        Specification<ServiceRequest> specification = getServiceRequestSpecification(jurisdictionId, serviceCodes, status, priority, startDate, endDate);

        return findAll(specification, pageable);
    }

    @Transactional
    default List<ServiceRequest> findAllBy(String jurisdictionId, List<Long> serviceCodes,
                                           List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,
                                           Instant startDate, Instant endDate, Sort sort) {

        Specification<ServiceRequest> specification = getServiceRequestSpecification(jurisdictionId, serviceCodes, status, priority, startDate, endDate);

        return findAll(specification, sort);
    }

    private static Specification<ServiceRequest> getServiceRequestSpecification(String jurisdictionId, List<Long> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate) {
        Specification<ServiceRequest> specification;
        specification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("jurisdiction_id"), jurisdictionId);

        if (serviceCodes != null && !serviceCodes.isEmpty()) {
            specification.and(Specifications.serviceCodeIn(serviceCodes));
        }

        if (status != null && !status.isEmpty()) {
            specification.and(Specifications.statusIn(status));
        }

        if (priority != null && !priority.isEmpty()) {
            specification.and(Specifications.priorityIn(priority));
        }

        if (startDate != null && endDate != null) {
            specification.and(Specifications.createdDateBetween(startDate, endDate));
        } else if (startDate != null && endDate == null) {
            specification.and(Specifications.createdDateAfter(startDate));
        } else if (startDate == null && endDate != null) {
            specification.and(Specifications.createdDateBefore(endDate));
        }
        return specification;
    }

    class Specifications {

        // serviceCode
        public static Specification<ServiceRequest> serviceCodeIn(List<Long> serviceCodes) {
            return (root, query, criteriaBuilder)
                    -> root.get("service_id").in(serviceCodes);
        }

        // status
        public static Specification<ServiceRequest> statusIn(List<ServiceRequestStatus> serviceRequestStatuses) {
            return (root, query, criteriaBuilder)
                    -> root.get("service_id").in(serviceRequestStatuses);
        }

        // priority
        public static Specification<ServiceRequest> priorityIn(List<ServiceRequestPriority> serviceRequestPriorities) {
            return (root, query, criteriaBuilder)
                    -> root.get("service_id").in(serviceRequestPriorities);
        }

        // dateCreated
        public static Specification<ServiceRequest> createdDateBetween(Instant startDate, Instant endDate) {
            return (root, query, criteriaBuilder)
                    -> criteriaBuilder.between(root.get(ServiceRequest_.dateCreated), startDate, endDate);
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
}
