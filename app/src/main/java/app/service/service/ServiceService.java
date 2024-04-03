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
import app.dto.service.PatchServiceOrderPositionDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.exception.Libre311BaseException;
import app.dto.servicedefinition.*;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.AttributeDataType;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicedefinition.*;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class ServiceService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceService.class);
    private final ServiceRepository serviceRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final ServiceGroupRepository serviceGroupRepository;
    private final ServiceDefinitionAttributeRepository serviceDefinitionAttributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    public ServiceService(ServiceRepository serviceRepository, JurisdictionRepository jurisdictionRepository, ServiceGroupRepository serviceGroupRepository, ServiceDefinitionAttributeRepository serviceDefinitionAttributeRepository, AttributeValueRepository attributeValueRepository) {
        this.serviceRepository = serviceRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.serviceGroupRepository = serviceGroupRepository;
        this.serviceDefinitionAttributeRepository = serviceDefinitionAttributeRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    static class ServiceNotFoundException extends Libre311BaseException {
        public ServiceNotFoundException(String serviceCode, String jurisdictionId) {
            super(String.format("No service found with serviceCode: %s for jurisdiction: %s",
                serviceCode, jurisdictionId), HttpStatus.NOT_FOUND);
        }

        public ServiceNotFoundException(Long serviceId, String jurisdictionId) {
            super(String.format("No service found with id: %s for jurisdiction: %s",
                serviceId, jurisdictionId), HttpStatus.NOT_FOUND);
        }
    }

    static class ServiceDefinitionAttributeNotFoundException extends Libre311BaseException {
        public ServiceDefinitionAttributeNotFoundException(Long attributeId) {
            super(String.format("No service definition attribute found with id: %s",
                    attributeId), HttpStatus.NOT_FOUND);
        }
    }

    static class MultiValueListServiceDefinitionNeedsValues extends Libre311BaseException {
        public MultiValueListServiceDefinitionNeedsValues() {
            super("Multi-value service definition attribute requires at least one value", HttpStatus.NOT_FOUND);
        }
    }

    static class GroupNotFoundException extends Libre311BaseException {
        public GroupNotFoundException(Long groupId) {
            super(String.format("No Group found with id: %s",
                    groupId), HttpStatus.NOT_FOUND);
        }
    }

    public Page<ServiceDTO> findAll(Pageable pageable, String jurisdictionId) {
        Page<Service> servicePage = serviceRepository.findAllByJurisdictionId(jurisdictionId, pageable);

        return servicePage.map(this::toServiceDTO);
    }

    public ServiceDefinitionDTO getServiceDefinition(Long serviceCode, String jurisdictionId) {
        Optional<Service> serviceOptional = serviceRepository.findById(serviceCode);

        if (serviceOptional.isEmpty()) {
            throw new ServiceNotFoundException(serviceCode, jurisdictionId);
        }
        Service service = serviceOptional.get();

        return convertToServiceDefinitionDTO(service);
    }

    public ServiceDTO createService(CreateServiceDTO serviceDTO, String jurisdictionId) {
        Jurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId).get();
        ServiceGroup group = validateGroupExistenceAndReturn(serviceDTO.getGroupId(), jurisdictionId);

        Service service = new Service();
        service.setJurisdiction(jurisdiction);
        service.setServiceGroup(group);
        service.setServiceName(serviceDTO.getServiceName());
        service.setDescription(serviceDTO.getDescription());
        if (serviceDTO.getOrderPosition() != null) {
            service.setOrderPosition(serviceDTO.getOrderPosition());
        }

        return toServiceDTO(serviceRepository.save(service));
    }

    public ServiceDTO updateService(Long serviceCode, UpdateServiceDTO serviceDTO, String jurisdictionId) {

        Optional<Service> serviceOptional = serviceRepository.findById(serviceCode);
        if (serviceOptional.isEmpty()) {
           throw new ServiceNotFoundException(serviceCode, jurisdictionId);
        }

        Service service = serviceOptional.get();
        if (serviceDTO.getGroupId() != null) {
            ServiceGroup group = validateGroupExistenceAndReturn(serviceDTO.getGroupId(), jurisdictionId);
            service.setServiceGroup(group);
        }
        if (serviceDTO.getDescription() != null) {
            service.setDescription(serviceDTO.getDescription());
        }
        if (serviceDTO.getServiceName() != null) {
            service.setServiceName(serviceDTO.getServiceName());
        }
        if (serviceDTO.getOrderPosition() != null) {
            service.setOrderPosition(serviceDTO.getOrderPosition());
        }

        return toServiceDTO(serviceRepository.update(service));
    }

    public List<ServiceDTO> updateServiceOrderPositions(Long groupId, List<PatchServiceOrderPositionDTO> requestDTO) {

        updateServicesOrderPosition(groupId, requestDTO);

        // get refreshed list of services
        List<Service> services = serviceRepository.findAllByServiceGroupIdOrderByOrderPositionAsc(groupId);
        return services.stream().map(this::toServiceDTO).collect(Collectors.toList());
    }

    @Transactional
    public void updateServicesOrderPosition(Long groupId, List<PatchServiceOrderPositionDTO> requestDTO) {
        requestDTO.forEach(patchServiceOrderPositionDTO -> serviceRepository.updateOrderPositionByIdAndServiceGroupId(
                patchServiceOrderPositionDTO.getServiceId(), groupId,
                patchServiceOrderPositionDTO.getOrderPosition()
        ));
    }

    public void deleteService(Long serviceCode, String jurisdictionId) {
        serviceRepository.findById(serviceCode)
            .orElseThrow(() -> new ServiceNotFoundException(serviceCode, jurisdictionId));
        serviceRepository.deleteById(serviceCode);
    }

    private ServiceGroup validateGroupExistenceAndReturn(Long groupId, String jurisdictionId) {
        Optional<ServiceGroup> serviceGroupOptional = serviceGroupRepository.findByIdAndJurisdictionId(groupId, jurisdictionId);
        return serviceGroupOptional.orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    public List<GroupDTO> getListGroups(String jurisdictionId) {
        return serviceGroupRepository.findAllByJurisdictionId(jurisdictionId).stream()
                .map(GroupDTO::new).collect(Collectors.toList());
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

    @Transactional
    public ServiceDefinitionDTO addServiceDefinitionAttributeToServiceDefinition(Long serviceId, CreateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO, String jurisdictionId) {
        Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
        if (serviceOptional.isEmpty()) {
            throw new ServiceNotFoundException(serviceId, jurisdictionId);
        }
        Service service = serviceOptional.get();

        return convertToServiceDefinitionDTO(addAttributeToServiceDefinition(serviceDefinitionAttributeDTO, service));
    }

    private ServiceDefinitionDTO convertToServiceDefinitionDTO(Service service) {
        ServiceDefinitionDTO serviceDefinitionDTO = new ServiceDefinitionDTO(service.getId());

        List<ServiceDefinitionAttribute> serviceDefinitionAttributes = serviceDefinitionAttributeRepository.findAllByServiceId(service.getId());
        if (serviceDefinitionAttributes != null) {
            serviceDefinitionDTO.setAttributes(serviceDefinitionAttributes.stream().map(serviceDefinitionAttributeEntity -> {
                ServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO = new ServiceDefinitionAttributeDTO(
                        serviceDefinitionAttributeEntity.getId(),
                        serviceDefinitionAttributeEntity.isVariable(),
                        serviceDefinitionAttributeEntity.getDatatype(),
                        serviceDefinitionAttributeEntity.isRequired(),
                        serviceDefinitionAttributeEntity.getDescription(),
                        serviceDefinitionAttributeEntity.getAttributeOrder(),
                        serviceDefinitionAttributeEntity.getDatatypeDescription());

                Set<AttributeValue> attributeValues = serviceDefinitionAttributeEntity.getAttributeValues();
                if (attributeValues != null) {
                    serviceDefinitionAttributeDTO.setValues(attributeValues.stream()
                            .map(attributeValueEntity -> new AttributeValueDTO(
                                    attributeValueEntity.getId().toString(),
                                    attributeValueEntity.getValueName()))
                            .collect(Collectors.toList()));
                }

                return serviceDefinitionAttributeDTO;
            }).collect(Collectors.toList()));
        }

        return serviceDefinitionDTO;
    }

    public ServiceDefinitionDTO updateServiceDefinitionAttribute(Long attributeCode, UpdateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO) {
        Optional<ServiceDefinitionAttribute> serviceDefinitionAttributeEntityOptional = serviceDefinitionAttributeRepository.findById(attributeCode);
        if (serviceDefinitionAttributeEntityOptional.isEmpty()) {
            throw new ServiceDefinitionAttributeNotFoundException(attributeCode);
        }
        ServiceDefinitionAttribute serviceDefinitionAttribute = serviceDefinitionAttributeEntityOptional.get();

        ServiceDefinitionAttribute patch = patchServiceDefinitionAttribute(serviceDefinitionAttribute, serviceDefinitionAttributeDTO);

        return convertToServiceDefinitionDTO(patch.getService());
    }

    @Transactional
    public ServiceDefinitionAttribute patchServiceDefinitionAttribute(ServiceDefinitionAttribute serviceDefinitionAttribute, UpdateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO) {

        if (serviceDefinitionAttributeDTO.isVariable() != null) {
            serviceDefinitionAttribute.setVariable(serviceDefinitionAttributeDTO.isVariable());
        }
        if (serviceDefinitionAttributeDTO.getDatatype() != null) {
            serviceDefinitionAttribute.setRequired(serviceDefinitionAttributeDTO.isRequired());
        }
        if (serviceDefinitionAttributeDTO.getDatatype() != null) {
            serviceDefinitionAttribute.setDatatype(serviceDefinitionAttributeDTO.getDatatype());
        }
        if (serviceDefinitionAttributeDTO.getDescription() != null) {
            serviceDefinitionAttribute.setDescription(serviceDefinitionAttributeDTO.getDescription());
        }
        if (serviceDefinitionAttributeDTO.getAttributeOrder() != null) {
            serviceDefinitionAttribute.setAttributeOrder(serviceDefinitionAttributeDTO.getAttributeOrder());
        }
        if (serviceDefinitionAttributeDTO.getDatatypeDescription() != null) {
            serviceDefinitionAttribute.setDatatypeDescription(serviceDefinitionAttributeDTO.getDatatypeDescription());
        }

        // add new values
        if (serviceDefinitionAttributeDTO.getValues() != null) {
            // drop all attribute values
            attributeValueRepository.deleteAllByServiceDefinitionAttribute(serviceDefinitionAttribute);

            serviceDefinitionAttributeDTO.getValues().forEach(attributeValueDTO -> {
                AttributeValue savedValue = attributeValueRepository.save(new AttributeValue(serviceDefinitionAttribute, attributeValueDTO.getName()));
                serviceDefinitionAttribute.addAttributeValue(savedValue);
            });
        }

        return serviceDefinitionAttributeRepository.update(serviceDefinitionAttribute);
    }

    public void removeServiceDefinitionAttributeFromServiceDefinition(Long attributeId) {
        Optional<ServiceDefinitionAttribute> serviceDefinitionAttribute = serviceDefinitionAttributeRepository.findById(attributeId);
        if (serviceDefinitionAttribute.isEmpty()) {
            throw new ServiceDefinitionAttributeNotFoundException(attributeId);
        }
        ServiceDefinitionAttribute serviceDefinitionAttributeEntity = serviceDefinitionAttribute.get();
        serviceDefinitionAttributeRepository.delete(serviceDefinitionAttributeEntity);
    }

    @Transactional
    public Service addAttributeToServiceDefinition(CreateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO, Service service) {
        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(service);
        serviceDefinitionAttribute.setVariable(serviceDefinitionAttributeDTO.isVariable());
        serviceDefinitionAttribute.setDatatype(serviceDefinitionAttributeDTO.getDatatype());
        serviceDefinitionAttribute.setRequired(serviceDefinitionAttributeDTO.isRequired());
        serviceDefinitionAttribute.setDescription(serviceDefinitionAttributeDTO.getDescription());
        serviceDefinitionAttribute.setAttributeOrder(serviceDefinitionAttributeDTO.getAttributeOrder());
        serviceDefinitionAttribute.setDatatypeDescription(serviceDefinitionAttributeDTO.getDatatypeDescription());

        ServiceDefinitionAttribute savedSDA = serviceDefinitionAttributeRepository.save(serviceDefinitionAttribute);

        if (serviceDefinitionAttributeDTO.getValues() != null) {
            serviceDefinitionAttributeDTO.getValues().forEach(attributeValueDTO -> {
                AttributeValue savedValue = attributeValueRepository.save(new AttributeValue(savedSDA, attributeValueDTO.getName()));
                savedSDA.addAttributeValue(savedValue);
            });
        } else if (serviceDefinitionAttributeDTO.getDatatype().equals(AttributeDataType.MULTIVALUELIST) ||
                serviceDefinitionAttributeDTO.getDatatype().equals(AttributeDataType.SINGLEVALUELIST)) {
            throw new MultiValueListServiceDefinitionNeedsValues();
        }

        serviceDefinitionAttributeRepository.update(savedSDA);

        service.addAttribute(savedSDA);
        return serviceRepository.update(service);
    }


    private ServiceDTO toServiceDTO(Service service){
        return new ServiceDTO(service, serviceDefinitionAttributeRepository.existsByServiceId(service.getId()));
    }
}
