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

import app.dto.group.GroupDTO;
import app.dto.group.CreateUpdateGroupDTO;
import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.service.servicedefinition.ServiceDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final ServiceGroupRepository serviceGroupRepository;

    public ServiceService(ServiceRepository serviceRepository, JurisdictionRepository jurisdictionRepository, ServiceGroupRepository serviceGroupRepository) {
        this.serviceRepository = serviceRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.serviceGroupRepository = serviceGroupRepository;
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

        if (groupDoesNotExists(serviceDTO.getGroupId(), jurisdiction, service)) return null;

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

        if (serviceDTO.getGroupId() != null) {
            if (groupDoesNotExists(serviceDTO.getGroupId(), service.getJurisdiction(), service)) return null;
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

    private boolean groupDoesNotExists(Long serviceDTO, Jurisdiction jurisdiction, Service service) {
        Optional<ServiceGroup> serviceGroupOptional = serviceGroupRepository.findByIdAndJurisdiction(serviceDTO, jurisdiction);
        if (serviceGroupOptional.isPresent()) {
            service.setServiceGroup(serviceGroupOptional.get());
        } else {
            LOG.error("Group not found.");
            return true;
        }
        return false;
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

    public Page<GroupDTO> getPageableGroups(Pageable pageable, String jurisdictionId) {
        return serviceGroupRepository.findAllByJurisdictionId(jurisdictionId, pageable).map(GroupDTO::new);
    }

    public GroupDTO createGroup(CreateUpdateGroupDTO requestDTO, String jurisdictionId) {
        Jurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId).get();
        if (groupAlreadyExists(requestDTO.getName(), jurisdiction)) {
            return null;
        }

        ServiceGroup group = new ServiceGroup();
        group.setJurisdiction(jurisdiction);
        group.setName(requestDTO.getName());

        return new GroupDTO(serviceGroupRepository.save(group));
    }

    public GroupDTO updateGroup(Long groupId, CreateUpdateGroupDTO requestDTO) {
        Optional<ServiceGroup> groupOptional = serviceGroupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            LOG.error("Group not found.");
            return null;
        }

        ServiceGroup group = groupOptional.get();
        if (requestDTO.getName() != null) {
            if (groupAlreadyExists(requestDTO.getName(), group.getJurisdiction())) {
                return null;
            }
            group.setName(requestDTO.getName());
        }

        return new GroupDTO(serviceGroupRepository.update(group));

    }

    private boolean groupAlreadyExists(String name, Jurisdiction jurisdiction) {
        boolean serviceCodeAlreadyExists = serviceGroupRepository.existsByNameAndJurisdiction(name, jurisdiction);
        if (serviceCodeAlreadyExists) {
            LOG.error("Group with name {} already exists.", name);
            return true;
        }
        return false;
    }

    public void deleteGroup(Long groupId) {
        Optional<ServiceGroup> groupOptional = serviceGroupRepository.findById(groupId);
        if (groupOptional.isPresent()) {
            ServiceGroup serviceGroup = groupOptional.get();
            if (serviceRepository.countByServiceGroup(serviceGroup) == 0) {
                serviceGroupRepository.delete(serviceGroup);
            }
        } else {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Cannot delete Group with existing Service associations.");
        }
    }
}
