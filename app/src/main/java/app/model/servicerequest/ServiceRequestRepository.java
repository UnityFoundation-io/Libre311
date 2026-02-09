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

import app.model.jurisdiction.Jurisdiction_;
import app.model.service.Service_;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.annotation.Where;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;

import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRequestRepository extends PageableRepository<ServiceRequest, Long>,
        JpaSpecificationExecutor<ServiceRequest> {

    Page<ServiceRequest> findByIdInAndJurisdictionId(List<Long> serviceRequestIds, String jurisdictionId, Pageable pageable);
    List<ServiceRequest> findByIdInAndJurisdictionId(List<Long> serviceRequestIds, String jurisdictionId, Sort sort);
    Optional<ServiceRequest> findByIdAndJurisdictionId(Long serviceRequestId, String jurisdictionId);
    Optional<ServiceRequest> findByClientRequestIdAndJurisdictionId(String clientRequestId, String jurisdictionId);

    @Query("update ServiceRequest sr set sr.deleted = true where sr.id = :id and sr.jurisdiction.id = :jurisdictionId and sr.deleted = false")
    Integer delete(Long id, String jurisdictionId);

    @Where("@.deleted = :deleted")
    Optional<ServiceRequest> findByIdAndDeleted(Long id, boolean deleted);
    @Transactional
    default Page<ServiceRequest> findAllBy(String jurisdictionId, List<Long> serviceCodes,
                                           List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,
                                           Instant startDate, Instant endDate, Pageable pageable) {

        QuerySpecification<ServiceRequest> specification = getServiceRequestSpecification(jurisdictionId, serviceCodes,
                status, priority, startDate, endDate);

        return findAll(specification, pageable);
    }

    @Transactional
    default List<ServiceRequest> findAllBy(String jurisdictionId, List<Long> serviceCodes,
                                           List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority,
                                           Instant startDate, Instant endDate, Sort sort) {

        QuerySpecification<ServiceRequest> specification = getServiceRequestSpecification(jurisdictionId, serviceCodes,
                status, priority, startDate, endDate);

        return findAll(specification, sort);
    }

    private static QuerySpecification<ServiceRequest> getServiceRequestSpecification(String jurisdictionId,
                                                                                     List<Long> serviceCodes,
                                                                                     List<ServiceRequestStatus> status,
                                                                                     List<ServiceRequestPriority> priority,
                                                                                     Instant startDate, Instant endDate) {
        QuerySpecification<ServiceRequest> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ServiceRequest_.jurisdiction).get(Jurisdiction_.id), jurisdictionId);

        specification = specification.and(Specifications.notDeleted());

        if (serviceCodes != null && !serviceCodes.isEmpty()) {
            specification = specification.and(Specifications.serviceCodeIn(serviceCodes));
        }

        if (status != null && !status.isEmpty()) {
            specification = specification.and(Specifications.statusIn(status));
        }

        if (priority != null && !priority.isEmpty()) {
            specification = specification.and(Specifications.priorityIn(priority));
        }

        if (startDate != null && endDate != null) {
            specification = specification.and(Specifications.createdDateBetween(startDate, endDate));
        } else if (startDate != null) {
            specification = specification.and(Specifications.createdDateAfter(startDate));
        } else if (endDate != null) {
            specification = specification.and(Specifications.createdDateBefore(endDate));
        }

        return specification;
    }

    class Specifications {

        public static QuerySpecification<ServiceRequest> notDeleted() {
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(ServiceRequest_.deleted), false);
        }

        // serviceCode
        public static QuerySpecification<ServiceRequest> serviceCodeIn(List<Long> serviceCodes) {
            return (root, query, criteriaBuilder) -> root.get(ServiceRequest_.service).get(Service_.id).in(serviceCodes);
        }

        // status
        public static QuerySpecification<ServiceRequest> statusIn(List<ServiceRequestStatus> serviceRequestStatuses) {
            return (root, query, criteriaBuilder) -> root.get(ServiceRequest_.status).in(serviceRequestStatuses);
        }

        // priority
        public static QuerySpecification<ServiceRequest> priorityIn(List<ServiceRequestPriority> serviceRequestPriorities) {
            return (root, query, criteriaBuilder) -> root.get(ServiceRequest_.priority).in(serviceRequestPriorities);
        }

        // dateCreated
        public static QuerySpecification<ServiceRequest> createdDateBetween(Instant startDate, Instant endDate) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(ServiceRequest_.dateCreated),
                    startDate, endDate);
        }

        public static QuerySpecification<ServiceRequest> createdDateAfter(Instant instant) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(ServiceRequest_.dateCreated), instant);
        }

        public static QuerySpecification<ServiceRequest> createdDateBefore(Instant instant) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(ServiceRequest_.dateCreated), instant);
        }
    }
}
