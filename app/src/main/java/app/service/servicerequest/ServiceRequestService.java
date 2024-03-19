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

package app.service.servicerequest;

import app.dto.download.DownloadRequestsArgumentsDTO;
import app.dto.download.DownloadServiceRequestDTO;
import app.dto.servicedefinition.AttributeValueDTO;
import app.dto.servicedefinition.ServiceDefinitionAttributeDTO;
import app.dto.servicerequest.*;
import app.model.service.AttributeDataType;
import app.exception.Libre311BaseException;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.servicedefinition.AttributeValue;
import app.model.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicedefinition.ServiceDefinitionAttributeRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.recaptcha.ReCaptchaService;
import app.security.Permission;
import app.security.UnityAuthService;
import app.service.storage.StorageUrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
public class ServiceRequestService {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestService.class);
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceDefinitionAttributeRepository attributeRepository;
    private final ReCaptchaService reCaptchaService;
    private final StorageUrlUtil storageUrlUtil;
    private final UnityAuthService unityAuthService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
                                 ServiceRepository serviceRepository, ServiceDefinitionAttributeRepository attributeRepository,
                                 ReCaptchaService reCaptchaService, StorageUrlUtil storageUrlUtil, UnityAuthService unityAuthService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRepository = serviceRepository;
        this.attributeRepository = attributeRepository;
        this.reCaptchaService = reCaptchaService;
        this.storageUrlUtil = storageUrlUtil;
        this.unityAuthService = unityAuthService;
    }

    static class InvalidServiceRequestException extends Libre311BaseException {
        public InvalidServiceRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    private static ServiceRequestDTO convertToDTO(ServiceRequest serviceRequest) {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO(serviceRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        String attributesJson = serviceRequest.getAttributesJson();
        if (attributesJson != null) {
            try {
                ServiceDefinitionAttributeDTO[] serviceDefinitionAttributeDTOS = objectMapper.readValue(attributesJson, ServiceDefinitionAttributeDTO[].class);
                serviceRequestDTO.setSelectedValues(List.of(serviceDefinitionAttributeDTOS));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return serviceRequestDTO;
    }

    public PostResponseServiceRequestDTO createServiceRequest(HttpRequest<?> request, PostRequestServiceRequestDTO serviceRequestDTO, String jurisdictionId) {
        reCaptchaService.verifyReCaptcha(serviceRequestDTO.getgRecaptchaResponse());

        if (!validMediaUrl(serviceRequestDTO.getMediaUrl())) {
            throw new InvalidServiceRequestException("Media URL is invalid.");
        }

        Optional<Service> serviceByServiceCodeOptional = serviceRepository.findByServiceCodeAndJurisdictionId(
                serviceRequestDTO.getServiceCode(), jurisdictionId);

        if (serviceByServiceCodeOptional.isEmpty()) {
            throw new InvalidServiceRequestException("Corresponding service is not found.");
        }

        if (!jurisdictionId.equals(serviceByServiceCodeOptional.get().getJurisdiction().getId())) {
            throw new InvalidServiceRequestException(
                "Mismatch between jurisdiction_id provided and Service's associated jurisdiction.");
        }

        // validate if a location is provided
        boolean latLongProvided = StringUtils.hasText(serviceRequestDTO.getLatitude()) &&
                StringUtils.hasText(serviceRequestDTO.getLongitude());

        if (!latLongProvided) {
            throw new InvalidServiceRequestException("Lat/long are required.");
        }

        // validate if additional attributes are required
        Service service = serviceByServiceCodeOptional.get();
        ServiceRequest serviceRequest = transformDtoToServiceRequest(serviceRequestDTO, service);
        List<ServiceDefinitionAttribute> serviceDefinitionAttributes = attributeRepository.findAllByServiceId(service.getId());
        if (!serviceDefinitionAttributes.isEmpty()) {
            List<ServiceDefinitionAttributeDTO> requestAttributes = buildUserResponseAttributesFromRequest(request, serviceDefinitionAttributes);
            if (!requestAttributesHasAllRequiredServiceDefinitionAttributes(serviceDefinitionAttributes, requestAttributes)) {
                throw new InvalidServiceRequestException("Submitted Service Request does not contain required attribute values.");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                serviceRequest.setAttributesJson(objectMapper.writeValueAsString(requestAttributes));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return new PostResponseServiceRequestDTO(serviceRequestRepository.save(serviceRequest));
    }

    private boolean validMediaUrl(String mediaUrl) {
        if (mediaUrl == null) return true;
        return mediaUrl.startsWith(storageUrlUtil.getBucketUrlString());
    }

    private boolean requestAttributesHasAllRequiredServiceDefinitionAttributes(List<ServiceDefinitionAttribute> serviceDefinitionAttributes, List<ServiceDefinitionAttributeDTO> requestAttributes) {
        // collect all required attributes
        List<String> requiredCodes = serviceDefinitionAttributes.stream()
                .filter(ServiceDefinitionAttribute::isRequired)
                .map(ServiceDefinitionAttribute::getCode)
                .collect(Collectors.toList());

        // for each attr, check if it exists in requestAttributes
        List<String> requestCodes = requestAttributes.stream()
                .map(ServiceDefinitionAttributeDTO::getCode)
                .collect(Collectors.toList());

        return requestCodes.containsAll(requiredCodes);
    }

    private List<ServiceDefinitionAttributeDTO> buildUserResponseAttributesFromRequest(HttpRequest<?> request, List<ServiceDefinitionAttribute> serviceDefinitionAttributes) {

        Argument<Map<String, String>> type = Argument.mapOf(String.class, String.class);
        Optional<Map<String, String>> body = request.getBody(type);

        List<ServiceDefinitionAttributeDTO> attributes = new ArrayList<>();
        if (body.isPresent()) {
            Map<String, String> map = body.get();
            map.forEach((k, v) -> {
                if (k.startsWith("attribute[")) {
                    String attributeCode = k.substring(k.indexOf("[") + 1, k.indexOf("]"));

                    // search for attribute by code in serviceDefinitionAttributes
                    Optional<ServiceDefinitionAttribute> serviceDefinitionAttributeOptional = serviceDefinitionAttributes.stream()
                            .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getCode().equals(attributeCode))
                            .findFirst();

                    // if attribute in request does not exist in db, then ignore
                    if (serviceDefinitionAttributeOptional.isEmpty()) {
                        return;
                    }
                    ServiceDefinitionAttribute serviceDefinitionAttribute = serviceDefinitionAttributeOptional.get();

                    // validate the value if necessary (number and dates)
                    if (v != null && !validValueType(v, serviceDefinitionAttribute.getDatatype())) {
                        String errorMsg = String.format("Provided value for attribute with code %s is invalid", attributeCode);
                        LOG.error(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }

                    ServiceDefinitionAttributeDTO sda = new ServiceDefinitionAttributeDTO();
                    sda.setCode(attributeCode);
                    sda.setAttributeOrder(serviceDefinitionAttribute.getAttributeOrder());
                    sda.setRequired(serviceDefinitionAttribute.isRequired());
                    sda.setVariable(serviceDefinitionAttribute.isVariable());
                    sda.setDatatype(serviceDefinitionAttribute.getDatatype());
                    sda.setDescription(serviceDefinitionAttribute.getDescription());

                    List<AttributeValueDTO> values = new ArrayList<>();

                    if (serviceDefinitionAttribute.getDatatype() == AttributeDataType.SINGLEVALUELIST ||
                            serviceDefinitionAttribute.getDatatype() == AttributeDataType.MULTIVALUELIST) {

                        Set<AttributeValue> attributeValues = serviceDefinitionAttribute.getAttributeValues();
                        if (attributeValues != null && v != null) {
                            if (v.contains(",") && serviceDefinitionAttribute.getDatatype() == AttributeDataType.MULTIVALUELIST){
                                String[] attrResKeys = v.split(",");
                                for (String attrResKey : attrResKeys) {
                                    values.add(new AttributeValueDTO(attrResKey, getAttributeValueName(attrResKey, attributeValues)));
                                }
                            }
                            else {
                                values.add(new AttributeValueDTO(v, getAttributeValueName(v, attributeValues)));
                            }
                        }
                    } else {
                        // we need a way to capture the user's response. We will do so by adding an attribute value where
                        // the key is the code and the value is the user's response.
                        values.add(new AttributeValueDTO(attributeCode, v));
                    }

                    sda.setValues(values);
                    attributes.add(sda);
                }
            });
        }

        return attributes;
    }

    private boolean validValueType(Object v, AttributeDataType datatype) {

        if (datatype == AttributeDataType.NUMBER || datatype == AttributeDataType.DATETIME){
            String value = (String) v;

            if (datatype == AttributeDataType.NUMBER) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException nfe) {
                    return false;
                }
            } else {
                try {
                    Instant.parse(value);
                } catch (DateTimeParseException dtpe) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    private String getAttributeValueName(String valueKey, Set<AttributeValue> attributeValues) {
        String valueName = null;
        Optional<AttributeValue> attributeValueOptional = attributeValues.stream()
                .filter(attributeValue -> attributeValue.getId().toString().equals(valueKey)).findFirst();

        if (attributeValueOptional.isPresent()) {
            valueName = attributeValueOptional.get().getValueName();
        }

        return valueName;
    }

    private ServiceRequest transformDtoToServiceRequest(PostRequestServiceRequestDTO serviceRequestDTO, Service service) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(service);
        serviceRequest.setJurisdiction(service.getJurisdiction());
        serviceRequest.setLatitude(serviceRequestDTO.getLatitude());
        serviceRequest.setLongitude(serviceRequestDTO.getLongitude());
        serviceRequest.setAddressString(serviceRequestDTO.getAddressString());
        serviceRequest.setAddressId(serviceRequestDTO.getAddressId());
        serviceRequest.setEmail(serviceRequestDTO.getEmail());
        serviceRequest.setDeviceId(serviceRequestDTO.getDeviceId());
        serviceRequest.setAccountId(serviceRequestDTO.getAccountId());
        serviceRequest.setFirstName(serviceRequestDTO.getFirstName());
        serviceRequest.setLastName(serviceRequestDTO.getLastName());
        serviceRequest.setPhone(serviceRequestDTO.getPhone());
        serviceRequest.setDescription(serviceRequestDTO.getDescription());
        serviceRequest.setMediaUrl(serviceRequestDTO.getMediaUrl());
        return serviceRequest;
    }

    public SensitiveServiceRequestDTO updateServiceRequest(Long serviceRequestId, PatchServiceRequestDTO serviceRequestDTO, String jurisdictionId) {
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);

        if (serviceRequestOptional.isEmpty()) {
            LOG.error("Could not find Service Request with id {} and jurisdiction id {}.", serviceRequestId, jurisdictionId);
            return null;
        }

        ServiceRequest serviceRequest = serviceRequestOptional.get();
        applyPatch(serviceRequestDTO, serviceRequest);

        return convertToSensitiveDTO(serviceRequestRepository.update(serviceRequest));
    }

    private static void applyPatch(PatchServiceRequestDTO serviceRequestDTO, ServiceRequest serviceRequest) {
        if (serviceRequestDTO.getStatus() != null) {
            serviceRequest.setStatus(serviceRequestDTO.getStatus());
        }
        if (serviceRequestDTO.getPriority() != null) {
            serviceRequest.setPriority(serviceRequestDTO.getPriority());
        }
        if (serviceRequestDTO.getAgency_email() != null) {
            serviceRequest.setAgencyEmail(serviceRequestDTO.getAgency_email());
        }
        if (serviceRequestDTO.getService_notice() != null) {
            serviceRequest.setServiceNotice(serviceRequestDTO.getService_notice());
        }
        if (serviceRequestDTO.getStatus_notes() != null) {
            serviceRequest.setStatusNotes(serviceRequestDTO.getStatus_notes());
        }
        if (serviceRequestDTO.getAgency_responsible() != null) {
            serviceRequest.setAgencyResponsible(serviceRequestDTO.getAgency_responsible());
        }
        if (serviceRequestDTO.getExpected_date() != null) {
            serviceRequest.setExpectedDate(serviceRequestDTO.getExpected_date());
        }
        if (serviceRequestDTO.getClosed_date() != null) {
            serviceRequest.setClosedDate(serviceRequestDTO.getClosed_date());
        }
        if (serviceRequestDTO.getStatus_notes() != null) {
            serviceRequest.setStatusNotes(serviceRequestDTO.getStatus_notes());
        }
    }

    private static SensitiveServiceRequestDTO convertToSensitiveDTO(ServiceRequest serviceRequest) {
        SensitiveServiceRequestDTO serviceRequestDTO = new SensitiveServiceRequestDTO(serviceRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        String attributesJson = serviceRequest.getAttributesJson();
        if (attributesJson != null) {
            try {
                ServiceDefinitionAttributeDTO[] serviceDefinitionAttributeDTOS = objectMapper.readValue(attributesJson, ServiceDefinitionAttributeDTO[].class);
                serviceRequestDTO.setSelectedValues(List.of(serviceDefinitionAttributeDTOS));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return serviceRequestDTO;
    }

    public Page<ServiceRequestDTO> findAll(GetServiceRequestsDTO requestDTO, String jurisdictionId,
            @Nullable String authorization) {

        boolean canViewSensitive = false;
        if (authorization != null){
            canViewSensitive = unityAuthService.isUserPermittedForJurisdictionAction(authorization, jurisdictionId,
                    List.of(Permission.LIBRE311_REQUEST_VIEW_SUBTENANT,
                            Permission.LIBRE311_REQUEST_VIEW_TENANT, Permission.LIBRE311_REQUEST_VIEW_SYSTEM));
        }

        Function<ServiceRequest, ServiceRequestDTO> mapper = canViewSensitive
                ? ServiceRequestService::convertToSensitiveDTO
                : ServiceRequestService::convertToDTO;


        return getServiceRequestPage(requestDTO, jurisdictionId).map(mapper);
    }

    private Page<ServiceRequest> getServiceRequestPage(GetServiceRequestsDTO requestDTO, String jurisdictionId) {
        String serviceRequestIds = requestDTO.getId();
        List<String> serviceCodes = requestDTO.getServiceCodes();
        List<ServiceRequestStatus> statuses = requestDTO.getStatuses();
        List<ServiceRequestPriority> priorities = requestDTO.getPriorities();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();
        Pageable pageable = requestDTO.getPageable();

        if(!pageable.isSorted()) {
            pageable = pageable.order("dateCreated", Sort.Order.Direction.DESC);
        }

        if (StringUtils.hasText(serviceRequestIds)) {
            List<Long> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
            return serviceRequestRepository.findByIdInAndJurisdictionId(requestIds, jurisdictionId, pageable);
        }

        return getJurisdictionServiceRequests(jurisdictionId, pageable, serviceCodes, statuses, priorities, startDate, endDate);
    }

    private Page<ServiceRequest> getJurisdictionServiceRequests(String jurisdictionId, Pageable pageable, List<String> serviceCodes, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate) {

        if (serviceCodes != null && status != null && priority != null) {
//            code-status-priority: 1-1-1
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedBetween(jurisdictionId, serviceCodes, status, priority, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedAfter(jurisdictionId, serviceCodes, status, priority, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityInAndDateCreatedBefore(jurisdictionId, serviceCodes, status, priority, endDate, pageable);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndPriorityIn(jurisdictionId, serviceCodes, status, priority, pageable);
        }

        if (priority == null) {
            if (serviceCodes != null && status != null) {
//                code-status-priority: 1-1-0
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedBetween(jurisdictionId, serviceCodes, status, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedAfter(jurisdictionId, serviceCodes, status, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusInAndDateCreatedBefore(jurisdictionId, serviceCodes, status, endDate, pageable);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndStatusIn(jurisdictionId, serviceCodes, status, pageable);
            } else if (serviceCodes != null && status == null) {
//                code-status-priority: 1-0-0
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedBetween(jurisdictionId, serviceCodes, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedAfter(jurisdictionId, serviceCodes, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndDateCreatedBefore(jurisdictionId, serviceCodes, endDate, pageable);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeIn(jurisdictionId, serviceCodes, pageable);
            } else if (serviceCodes == null && status != null) {
//            code-status-priority: 0-1-0
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedBetween(jurisdictionId, status, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedAfter(jurisdictionId, status, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedBefore(jurisdictionId, status, endDate, pageable);
                }

                return serviceRequestRepository.findByJurisdictionIdAndStatusIn(jurisdictionId, status, pageable);
            }
        } else {
            if (serviceCodes != null && status == null) {
//                code-status-priority: 1-0-1
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedBetween(jurisdictionId, serviceCodes, priority, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedAfter(jurisdictionId, serviceCodes, priority, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndPriorityInAndDateCreatedBefore(jurisdictionId, serviceCodes, priority, endDate, pageable);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeInAndPriorityIn(jurisdictionId, serviceCodes, priority, pageable);
            } else if (serviceCodes == null && status != null) {
//            code-status-priority: 0-1-1
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(jurisdictionId, status, priority, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(jurisdictionId, status, priority, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(jurisdictionId, status, priority, endDate, pageable);
                }

                return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityIn(jurisdictionId, status, priority, pageable);
            } else if (serviceCodes == null && status == null) {
//            code-status-priority: 0-0-1
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedBetween(jurisdictionId, priority, startDate, endDate, pageable);
                } else if (startDate != null && endDate == null) {
                    // just start
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedAfter(jurisdictionId, priority, startDate, pageable);
                } else if (startDate == null && endDate != null) {
                    // just end
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedBefore(jurisdictionId, priority, endDate, pageable);
                }

                return serviceRequestRepository.findAllByJurisdictionIdAndPriorityIn(jurisdictionId, priority, pageable);
            }
        }

//        code-status-priority: 0-0-0
        if (startDate != null && endDate != null) {
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedBetween(jurisdictionId, startDate, endDate, pageable);
        } else if (startDate != null && endDate == null) {
            // just start
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedAfter(jurisdictionId, startDate, pageable);
        } else if (startDate == null && endDate != null) {
            // just end
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedBefore(jurisdictionId, endDate, pageable);
        }

        return serviceRequestRepository.findAllByJurisdictionId(jurisdictionId, pageable);
    }

    public ServiceRequestDTO getServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return findServiceRequest(serviceRequestId, jurisdictionId)
                .map(ServiceRequestService::convertToDTO)
                .orElse(null);
    }

    public SensitiveServiceRequestDTO getSensitiveServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return findServiceRequest(serviceRequestId, jurisdictionId)
                .map(ServiceRequestService::convertToSensitiveDTO)
                .orElse(null);
    }

    private Optional<ServiceRequest> findServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);
    }

    public StreamedFile getAllServiceRequests(DownloadRequestsArgumentsDTO downloadRequestsArgumentsDTO, String jurisdictionId) throws MalformedURLException {

        List<DownloadServiceRequestDTO> downloadServiceRequestDTOS = getServiceRequests(downloadRequestsArgumentsDTO, jurisdictionId).stream()
                .map(serviceRequest -> {
                    DownloadServiceRequestDTO dto = new DownloadServiceRequestDTO(serviceRequest);

                    if (serviceRequest.getAttributesJson() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            ServiceDefinitionAttributeDTO[] serviceDefinitionAttributeDTOS =
                                    objectMapper.readValue(serviceRequest.getAttributesJson(), ServiceDefinitionAttributeDTO[].class);
                            List<String> values = Arrays.stream(serviceDefinitionAttributeDTOS)
                                    .flatMap(serviceDefinitionAttribute -> {
                                        if (serviceDefinitionAttribute.getValues() != null) {
                                            return serviceDefinitionAttribute.getValues().stream();
                                        }
                                        return Stream.of();
                                    })
                                    .map(AttributeValueDTO::getKey).collect(Collectors.toList());

                            dto.setServiceSubtype(String.join(",", values));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());


        File tmpFile;
        Instant now = Instant.now();
        try {
            tmpFile = File.createTempFile(now.toString(), ".csv");
            try (Writer writer  = new FileWriter(tmpFile)) {

                StatefulBeanToCsv<DownloadServiceRequestDTO> sbc = new StatefulBeanToCsvBuilder<DownloadServiceRequestDTO>(writer)
                        .build();

                sbc.write(downloadServiceRequestDTOS);
            }
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            throw new RuntimeException(e);
        }

        return new StreamedFile(tmpFile.toURI().toURL()).attach(now + ".csv");
    }

    private List<ServiceRequest> getServiceRequests(DownloadRequestsArgumentsDTO requestDTO, String jurisdictionId) {
        String serviceName = requestDTO.getServiceName();
        List<ServiceRequestStatus> statuses = requestDTO.getStatuses();
        List<ServiceRequestPriority> priorities = requestDTO.getPriorities();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();

        Optional<ServiceRequest> byServiceName;
        if (serviceName == null) {
            byServiceName = Optional.empty();
        } else {
            byServiceName = serviceRequestRepository.findByServiceServiceNameIlikeAndJurisdictionId(serviceName, jurisdictionId);
        }

        return getJurisdictionServiceRequests(jurisdictionId, byServiceName, statuses, priorities, startDate, endDate);
    }

    private List<ServiceRequest> getJurisdictionServiceRequests(String jurisdictionId, Optional<ServiceRequest> byServiceName, List<ServiceRequestStatus> status, List<ServiceRequestPriority> priority, Instant startDate, Instant endDate) {
        String serviceCode;
        if (byServiceName.isPresent() && status != null && priority != null) {
//            code-status-priority: 1-1-1
            serviceCode = byServiceName.get().getService().getServiceCode();
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedBetween(jurisdictionId, serviceCode, status, priority, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedAfter(jurisdictionId, serviceCode, status, priority, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityInAndDateCreatedBefore(jurisdictionId, serviceCode, status, priority, endDate);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndPriorityIn(jurisdictionId, serviceCode, status, priority);
        }

        if (priority == null) {
            if (byServiceName.isPresent() && status != null) {
//                code-status-priority: 1-1-0
                serviceCode = byServiceName.get().getService().getServiceCode();
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedBetween(jurisdictionId, serviceCode, status, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedAfter(jurisdictionId, serviceCode, status, startDate);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusInAndDateCreatedBefore(jurisdictionId, serviceCode, status, endDate);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusIn(jurisdictionId, serviceCode, status);
            } else if (byServiceName.isPresent() && status == null) {
//                code-status-priority: 1-0-0
                serviceCode = byServiceName.get().getService().getServiceCode();
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(jurisdictionId, serviceCode, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(jurisdictionId, serviceCode, startDate);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(jurisdictionId, serviceCode, endDate);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCode(jurisdictionId, serviceCode);
            } else if (byServiceName.isEmpty() && status != null) {
//                code-status-priority: 0-1-0
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedBetween(jurisdictionId, status, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedAfter(jurisdictionId, status, startDate);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndDateCreatedBefore(jurisdictionId, status, endDate);
                }

                return serviceRequestRepository.findByJurisdictionIdAndStatusIn(jurisdictionId, status);
            }
        } else {
            if (byServiceName.isPresent() && status != null) {
//                code-status-priority: 0-1-1
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBetween(jurisdictionId, status, priority, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedAfter(jurisdictionId, status, priority, startDate);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityInAndDateCreatedBefore(jurisdictionId, status, priority, endDate);
                }

                return serviceRequestRepository.findByJurisdictionIdAndStatusInAndPriorityIn(jurisdictionId, status, priority);
            } else if (byServiceName.isPresent() && status == null) {
//                code-status-priority: 1-0-1
                serviceCode = byServiceName.get().getService().getServiceCode();
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedBetween(jurisdictionId, serviceCode, priority, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedAfter(jurisdictionId, serviceCode, priority, startDate);
                } else if (startDate == null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndPriorityInAndDateCreatedBefore(jurisdictionId, serviceCode, priority, endDate);
                }

                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndPriorityIn(jurisdictionId, serviceCode, priority);
            } else if (byServiceName.isEmpty() && status != null) {
//                  code-status-priority: 0-0-1
                if (startDate != null && endDate != null) {
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedBetween(jurisdictionId, priority, startDate, endDate);
                } else if (startDate != null && endDate == null) {
                    // just start
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedAfter(jurisdictionId, priority, startDate);
                } else if (startDate == null && endDate != null) {
                    // just end
                    return serviceRequestRepository.findByJurisdictionIdAndPriorityInAndDateCreatedBefore(jurisdictionId, priority, endDate);
                }

                return serviceRequestRepository.findAllByJurisdictionIdAndPriorityIn(jurisdictionId, priority);
            }
        }

//        code-status-priority: 0-0-0
        if (startDate != null && endDate != null) {
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedBetween(jurisdictionId, startDate, endDate);
        } else if (startDate != null && endDate == null) {
            // just start
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedAfter(jurisdictionId, startDate);
        } else if (startDate == null && endDate != null) {
            // just end
            return serviceRequestRepository.findByJurisdictionIdAndDateCreatedBefore(jurisdictionId, endDate);
        }

        return serviceRequestRepository.findAllByJurisdictionId(jurisdictionId);
    }
}
