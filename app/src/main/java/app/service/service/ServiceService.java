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

package app.service.service;

import app.dto.service.ServiceDTO;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.service.storage.StorageService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Page<ServiceDTO> findAll(Pageable pageable) {
        return serviceRepository.findAll(pageable).map(ServiceDTO::new);
    }

    public String getServiceDefinition(String serviceCode) {
        Optional<Service> byServiceCode = serviceRepository.findByServiceCode(serviceCode);

        if (byServiceCode.isEmpty()) {
            LOG.error("Corresponding service not found.");
            return null;
        } else if (byServiceCode.get().getServiceDefinitionJson() == null) {
            LOG.error("Service Definition is null.");
            return null;
        }

        return byServiceCode.get().getServiceDefinitionJson();
    }
}
