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

package app.util;

import app.model.jurisdiction.JurisdictionBoundaryRepository;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdictionuser.JurisdictionUserRepository;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicedefinition.AttributeValueRepository;
import app.model.servicedefinition.ServiceDefinitionAttributeRepository;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.user.UserRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;

@Singleton
public class DbCleanup {

    @Inject
    public JurisdictionRepository jurisdictionRepository;

    @Inject
    public ServiceGroupRepository serviceGroupRepository;

    @Inject
    public ServiceRepository serviceRepository;

    @Inject
    public ServiceRequestRepository serviceRequestRepository;
    @Inject
    public ServiceDefinitionAttributeRepository serviceDefinitionAttributeRepository;

    @Inject
    public AttributeValueRepository attributeValueRepository;

    @Inject
    public JurisdictionBoundaryRepository jurisdictionBoundaryRepository;

    @Inject
    public UserRepository userRepository;

    @Inject
    public JurisdictionUserRepository jurisdictionUserRepository;

    @Transactional
    public void cleanupAll(){
        userRepository.deleteAll();
        jurisdictionUserRepository.deleteAll();
        attributeValueRepository.deleteAll();
        serviceDefinitionAttributeRepository.deleteAll();
        serviceRepository.deleteAll();
        serviceGroupRepository.deleteAll();
        jurisdictionRepository.deleteAll();
        serviceRequestRepository.deleteAll();
    }

    @Transactional
    public void cleanupServiceRequests() {
        serviceRequestRepository.deleteAll();
    }

}
