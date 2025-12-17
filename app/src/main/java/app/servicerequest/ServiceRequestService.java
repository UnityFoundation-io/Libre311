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

package app.servicerequest;

import app.servicedefinition.ServiceDefinitionAttributeDTO;
import app.exception.Libre311BaseException;
import app.servicedefinition.ServiceDefinition;
import app.servicedefinition.ServiceDefinitionAttribute;
import app.servicedefinition.ServiceDefinitionAttributeRepository;
import app.security.Permission;
import app.security.UnityAuthService;
import app.servicerequest.download.CsvGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Singleton
public class ServiceRequestService {

    static class InvalidServiceRequestException extends Libre311BaseException {
        public InvalidServiceRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestService.class);
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceDefinitionAttributeRepository attributeRepository;
    private final UnityAuthService unityAuthService;
    private final ServiceRequestMapper serviceRequestMapper;
    private final CsvGenerator csvGenerator;
    private final ServiceRequestValidator serviceRequestValidator;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 ServiceDefinitionAttributeRepository attributeRepository,
                                 UnityAuthService unityAuthService,
                                 ServiceRequestMapper serviceRequestMapper,
                                 CsvGenerator csvGenerator,
                                 ServiceRequestValidator serviceRequestValidator) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.attributeRepository = attributeRepository;
        this.unityAuthService = unityAuthService;
        this.serviceRequestMapper = serviceRequestMapper;
        this.csvGenerator = csvGenerator;
        this.serviceRequestValidator = serviceRequestValidator;
    }

    public ServiceRequestPostResponse createServiceRequest(HttpRequest<?> request, ServiceRequestPostRequest serviceRequestDTO, String jurisdictionId) {

        LOG.debug(serviceRequestDTO.toString());

        serviceRequestValidator.validateRequestInBoundaries(serviceRequestDTO, jurisdictionId);
        serviceRequestValidator.validMediaUrl(serviceRequestDTO.getMediaUrl());
        ServiceDefinition serviceDefinition = serviceRequestValidator.validService(serviceRequestDTO, jurisdictionId);

        // validate if additional attributes are required
        ServiceRequest serviceRequest = serviceRequestMapper.toServiceRequest(serviceRequestDTO, serviceDefinition);
        List<ServiceDefinitionAttribute> serviceDefinitionAttributes = attributeRepository.findAllByServiceId(serviceDefinition.getId());
        if (!serviceDefinitionAttributes.isEmpty()) {
            List<ServiceDefinitionAttributeDTO> requestAttributes = serviceRequestMapper.mapAttributeDtos(serviceRequestDTO.getAttributes(), serviceDefinitionAttributes);
            serviceRequestValidator.requiredAttributesPresent(serviceDefinitionAttributes, requestAttributes);

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                serviceRequest.setAttributesJson(objectMapper.writeValueAsString(requestAttributes));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return new ServiceRequestPostResponse(serviceRequestRepository.save(serviceRequest));
    }

    public SensitiveServiceRequestDTO updateServiceRequest(Long serviceRequestId, ServiceRequestUpdateRequest updateRequest, String jurisdictionId) {
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);

        if (serviceRequestOptional.isEmpty()) {
            LOG.error("Could not find Service Request with id {} and jurisdiction id {}.", serviceRequestId, jurisdictionId);
            return null;
        }

        ServiceRequest serviceRequest = serviceRequestOptional.get();
        serviceRequestMapper.applyPatch(updateRequest, serviceRequest);

        return serviceRequestMapper.toSensitiveDto(serviceRequestRepository.update(serviceRequest));
    }

    public Page<ServiceRequestDTO> findAll(ServiceRequestListRequest requestDTO, String jurisdictionId,
                                           @Nullable String authorization) {

        boolean canViewSensitive = false;
        if (authorization != null) {
            canViewSensitive = unityAuthService.isUserPermittedForJurisdictionAction(authorization, jurisdictionId,
                    List.of(Permission.LIBRE311_REQUEST_VIEW_SUBTENANT,
                            Permission.LIBRE311_REQUEST_VIEW_TENANT, Permission.LIBRE311_REQUEST_VIEW_SYSTEM));
        }

        Function<ServiceRequest, ServiceRequestDTO> mapper = canViewSensitive
                ? serviceRequestMapper::toSensitiveDto
                : serviceRequestMapper::toDto;


        return getServiceRequestPage(requestDTO, jurisdictionId).map(mapper);
    }

    private Page<ServiceRequest> getServiceRequestPage(ServiceRequestListRequest requestDTO, String jurisdictionId) {
        String serviceRequestIds = requestDTO.getId();
        Pageable pageable = requestDTO.getPageable();

        if (!pageable.isSorted()) {
            pageable = pageable.order("dateCreated", Sort.Order.Direction.DESC);
        }

        if (StringUtils.hasText(serviceRequestIds)) {
            List<Long> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
            return serviceRequestRepository.findByIdInAndJurisdictionId(requestIds, jurisdictionId, pageable);
        }

        return serviceRequestRepository.findAllBy(jurisdictionId, requestDTO.getServiceCodes(), requestDTO.getStatuses(), requestDTO.getPriorities(), requestDTO.getStartDate(), requestDTO.getEndDate(), pageable);
    }

    public ServiceRequestDTO getServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId)
                .map(serviceRequestMapper::toDto)
                .orElse(null);
    }

    public StreamedFile getAllServiceRequests(ServiceRequestListRequest requestDTO, String jurisdictionId) throws MalformedURLException {
        return csvGenerator.generateRequestListCsv(getServiceRequests(requestDTO, jurisdictionId));
    }

    private List<ServiceRequest> getServiceRequests(ServiceRequestListRequest requestDTO, String jurisdictionId) {
        Pageable pageable = requestDTO.getPageable();

        Sort sort;
        if (pageable != null && pageable.isSorted()) {
            sort = pageable.getSort();
        } else {
            sort = Sort.of(new Sort.Order("dateCreated", Sort.Order.Direction.DESC, false));
        }

        if (StringUtils.hasText(requestDTO.getId())) {
            return serviceRequestRepository.findByIdInAndJurisdictionId(requestDTO.parsedIds(), jurisdictionId, sort);
        }

        return serviceRequestRepository.findAllBy(jurisdictionId, requestDTO.getServiceCodes(), requestDTO.getStatuses(), requestDTO.getPriorities(), requestDTO.getStartDate(), requestDTO.getEndDate(), sort);
    }

    public int delete(Long serviceRequestId, String jurisdictionId) {
        return serviceRequestRepository.updateDeletedByIdAndJurisdictionIdAndDeletedFalse(serviceRequestId, jurisdictionId, true);
    }
}
