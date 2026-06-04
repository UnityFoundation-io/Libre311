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

package app.service;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

@Singleton
public class SystemReservedGroupInitializer {

    public static final String SYSTEM_RESERVED = "System Reserved";

    private final JurisdictionRepository jurisdictionRepository;
    private final ServiceGroupRepository serviceGroupRepository;

    public SystemReservedGroupInitializer(JurisdictionRepository jurisdictionRepository,
                                          ServiceGroupRepository serviceGroupRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
        this.serviceGroupRepository = serviceGroupRepository;
    }

    @EventListener
    @Transactional
    public void onStartup(ServerStartupEvent event) {
        for (Jurisdiction jurisdiction : jurisdictionRepository.findAll()) {
            if (!serviceGroupRepository.existsByNameAndJurisdiction(SYSTEM_RESERVED, jurisdiction)) {
                serviceGroupRepository.save(new ServiceGroup(SYSTEM_RESERVED, jurisdiction));
            }
        }
    }
}
