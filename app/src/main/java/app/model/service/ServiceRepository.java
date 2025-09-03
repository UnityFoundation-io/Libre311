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

import app.model.service.group.ServiceGroup;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;

@Repository
public interface ServiceRepository extends PageableRepository<Service, Long> {
    List<Service> findAllByJurisdictionIdOrderByOrderPosition(String jurisdictionId);
    List<Service> findAllByServiceGroupIdOrderByOrderPositionAsc(Long groupId);
    void updateOrderPositionByIdAndServiceGroupId(@Id Long id, @Id Long groupId, Integer orderPosition);
    Long countByServiceGroup(ServiceGroup serviceGroup);
}
