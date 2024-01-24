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

import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.servicedefinition.ServiceDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;
    private final JurisdictionRepository jurisdictionRepository;

    public ServiceService(ServiceRepository serviceRepository, JurisdictionRepository jurisdictionRepository) {
        this.serviceRepository = serviceRepository;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public Page<ServiceDTO> findAll(Pageable pageable, String jurisdictionId) {
        Page<Service> servicePage = serviceRepository.findAllByJurisdictionId(jurisdictionId, pageable);

        return servicePage.map(ServiceDTO::new);
    }

    public String getServiceDefinition(String serviceCode, String jurisdictionId) {
        Optional<Service> serviceOptional = serviceRepository.findByServiceCodeAndJurisdictionId(serviceCode, jurisdictionId);

        if (serviceOptional.isEmpty()) {
            LOG.error("Service not found.");
            return null;
        } else if (serviceOptional.get().getServiceDefinitionJson() == null) {
            LOG.error("Service Definition is null.");
            return null;
        }

        return serviceOptional.get().getServiceDefinitionJson();
    }

    public ServiceDTO createService(CreateServiceDTO serviceDTO, String jurisdictionId) {
        Jurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId).get();
        if (serviceCodeAlreadyExists(serviceDTO.getServiceCode(), jurisdiction)) {
            return null;
        }

        Service service = new Service();

        if (serviceDTO.getServiceDefinitionJson() != null) {
            if (serviceDefinitionFormatIsValid(serviceDTO.getServiceDefinitionJson())) {
                service.setMetadata(true);
                service.setServiceDefinitionJson(serviceDTO.getServiceDefinitionJson());
            } else {
                LOG.error("Service Definition JSON format is invalid.");
                return null;
            }
        }
        service.setJurisdiction(jurisdiction);
        service.setServiceCode(serviceDTO.getServiceCode());
        service.setServiceName(serviceDTO.getServiceName());
        service.setDescription(serviceDTO.getDescription());

        return new ServiceDTO(serviceRepository.save(service));
    }

    public ServiceDTO updateService(Long serviceId, UpdateServiceDTO serviceDTO, String jurisdictionId) {

        Optional<Service> serviceOptional = serviceRepository.findByIdAndJurisdictionId(serviceId, jurisdictionId);
        if (serviceOptional.isEmpty()) {
            LOG.error("Service not found.");
            return null;
        }

        Service service = serviceOptional.get();
        if (serviceDTO.getServiceCode() != null) {
            if (serviceCodeAlreadyExists(serviceDTO.getServiceCode(), service.getJurisdiction())) {
                return null;
            }
            service.setServiceCode(serviceDTO.getServiceCode());
        }

        if (serviceDTO.getDescription() != null) {
            service.setDescription(serviceDTO.getDescription());
        }
        if (serviceDTO.getServiceName() != null) {
            service.setServiceName(serviceDTO.getServiceName());
        }
        if (serviceDTO.getServiceDefinitionJson() != null) {
            if (serviceDefinitionFormatIsValid(serviceDTO.getServiceDefinitionJson())) {
                service.setMetadata(true);
                service.setServiceDefinitionJson(serviceDTO.getServiceDefinitionJson());
            } else {
                LOG.error("Service Definition JSON format is invalid.");
                return null;
            }
        }

        return new ServiceDTO(serviceRepository.update(service));
    }

    private boolean serviceCodeAlreadyExists(String serviceCode, Jurisdiction jurisdiction) {
        boolean serviceCodeAlreadyExists = serviceRepository.existsByServiceCodeAndJurisdiction(serviceCode, jurisdiction);
        if (serviceCodeAlreadyExists) {
            LOG.error("Service with code {} already exists.", serviceCode);
            return true;
        }
        return false;
    }

    private boolean serviceDefinitionFormatIsValid(String serviceDefinitionJson) {
        try {
            (new ObjectMapper()).readValue(serviceDefinitionJson, ServiceDefinition.class);
        } catch (JsonProcessingException e) {
            return false;
        }
        return true;
    }
}
