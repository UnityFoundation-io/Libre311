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

package app.model.service;

import app.model.jurisdiction.Jurisdiction;
import app.model.service.group.ServiceGroup;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends PageableRepository<Service, Long> {
    Page<Service> findAllByJurisdictionId(String jurisdictionId, Pageable pageable);
    Optional<Service> findByServiceCodeAndJurisdictionId(String serviceCode, String jurisdictionId);
    Optional<Service> findByIdAndJurisdictionId(Long id, String jurisdictionId);
    boolean existsByServiceCodeAndJurisdiction(String serviceCode, Jurisdiction jurisdiction);
    List<Service> findByServiceGroup(ServiceGroup serviceGroup);
}
