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

import app.model.jurisdiction.JurisdictionRepository;
import app.model.servicerequest.ServiceRequestRepository;
import jakarta.inject.Singleton;

import jakarta.transaction.Transactional;

@Singleton
public class DbCleanup {

    private final ServiceRequestRepository serviceRequestRepository;
    private final JurisdictionRepository jurisdictionRepository;

    public DbCleanup(ServiceRequestRepository serviceRequestRepository, JurisdictionRepository jurisdictionRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @Transactional
    public void cleanupServiceRequests() {
        serviceRequestRepository.deleteAll();
    }

    @Transactional
    public void cleanupJurisdictions() {
        jurisdictionRepository.deleteAll();
    }
}
