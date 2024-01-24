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
import app.dto.servicerequest.*;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.servicedefinition.AttributeDataType;
import app.model.service.servicedefinition.AttributeValue;
import app.model.service.servicedefinition.ServiceDefinition;
import app.model.service.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.recaptcha.ReCaptchaService;
import app.service.storage.StorageUrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
    private final ReCaptchaService reCaptchaService;
    private final StorageUrlUtil storageUrlUtil;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository, ServiceRepository serviceRepository, ReCaptchaService reCaptchaService, StorageUrlUtil storageUrlUtil) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRepository = serviceRepository;
        this.reCaptchaService = reCaptchaService;
        this.storageUrlUtil = storageUrlUtil;
    }

    private static ServiceRequestDTO convertToDTO(ServiceRequest serviceRequest) {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO(serviceRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        String attributesJson = serviceRequest.getAttributesJson();
        if (attributesJson != null) {
            try {
                ServiceDefinitionAttribute[] serviceDefinitionAttributes = objectMapper.readValue(attributesJson, ServiceDefinitionAttribute[].class);
                serviceRequestDTO.setSelectedValues(List.of(serviceDefinitionAttributes));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return serviceRequestDTO;
    }

    public PostResponseServiceRequestDTO createServiceRequest(HttpRequest<?> request, PostRequestServiceRequestDTO serviceRequestDTO, String jurisdictionId) {
        if (!reCaptchaService.verifyReCaptcha(serviceRequestDTO.getgRecaptchaResponse())) {
            LOG.error("ReCaptcha verification failed.");
            return null;
        }

        if (!validMediaUrl(serviceRequestDTO.getMediaUrl())) {
            LOG.error("Media URL is invalid.");
            return null;
        }

        Optional<Service> serviceByServiceCodeOptional = serviceRepository.findByServiceCodeAndJurisdictionId(
                serviceRequestDTO.getServiceCode(), jurisdictionId);

        if (serviceByServiceCodeOptional.isEmpty()) {
            LOG.error("Corresponding service not found.");
            return null; // todo return 'corresponding service not found
        }

        if (!jurisdictionId.equals(serviceByServiceCodeOptional.get().getJurisdiction().getId())) {
            LOG.error("Mismatch between jurisdiction_id provided and Service's associated jurisdiction.");
            return null;
        }

        // validate if a location is provided
        boolean latLongProvided = StringUtils.hasText(serviceRequestDTO.getLatitude()) &&
                StringUtils.hasText(serviceRequestDTO.getLongitude());

        if (!latLongProvided &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressString()) &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressId())) {
            LOG.error("Address or lat/long not provided.");
            return null; // todo throw exception
        }

        // validate if additional attributes are required
        List<ServiceDefinitionAttribute> requestAttributes = null;
        Service service = serviceByServiceCodeOptional.get();
        if (service.isMetadata()) {
            // get service definition
            String serviceDefinitionJson = service.getServiceDefinitionJson();
            if (serviceDefinitionJson == null || serviceDefinitionJson.isBlank()) {
                LOG.error("Service definition does not exists despite service requiring it.");
                return null; // should not be in this state and admin needs to be aware.
            }

            requestAttributes = buildUserResponseAttributesFromRequest(request, serviceDefinitionJson);
            if (requestAttributes.isEmpty()) {
                LOG.error("Submitted Service Request does not contain any attribute values.");
                return null; // todo throw exception - must provide attributes
            }
            if (!requestAttributesHasAllRequiredServiceDefinitionAttributes(serviceDefinitionJson, requestAttributes)) {
                LOG.error("Submitted Service Request does not contain required attribute values.");
                return null; // todo throw exception (validation)
            }
        }

        ServiceRequest serviceRequest = transformDtoToServiceRequest(serviceRequestDTO, service);
        if (requestAttributes != null) {
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

    private boolean requestAttributesHasAllRequiredServiceDefinitionAttributes(String serviceDefinitionJson, List<ServiceDefinitionAttribute> requestAttributes) {
        // deserialize
        ObjectMapper objectMapper = new ObjectMapper();
        boolean containsAllRequiredAttrs = false;
        try {
            // collect all required attributes
            ServiceDefinition serviceDefinition = objectMapper.readValue(serviceDefinitionJson, ServiceDefinition.class);
            List<String> requiredCodes = serviceDefinition.getAttributes().stream()
                    .filter(ServiceDefinitionAttribute::isRequired)
                    .map(ServiceDefinitionAttribute::getCode)
                    .collect(Collectors.toList());

            // for each attr, check if it exists in requestAttributes
            List<String> requestCodes = requestAttributes.stream()
                    .map(ServiceDefinitionAttribute::getCode)
                    .collect(Collectors.toList());
            containsAllRequiredAttrs = requestCodes.containsAll(requiredCodes);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return containsAllRequiredAttrs;
    }

    private List<ServiceDefinitionAttribute> buildUserResponseAttributesFromRequest(HttpRequest<?> request, String serviceDefinitionJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceDefinition serviceDefinition;
        try {
            serviceDefinition = objectMapper.readValue(serviceDefinitionJson, ServiceDefinition.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Optional<Map> body = request.getBody(Map.class);

        List<ServiceDefinitionAttribute> attributes = new ArrayList<>();
        if (body.isPresent()) {
            Map<String, Object> map = body.get();
            map.forEach((k, v) -> {
                if (k.startsWith("attribute[")) {
                    String attributeCode = k.substring(k.indexOf("[") + 1, k.indexOf("]"));

                    // search for attribute by code in serviceDefinition
                    Optional<ServiceDefinitionAttribute> serviceDefinitionAttributeOptional = serviceDefinition.getAttributes().stream()
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

                    ServiceDefinitionAttribute sda = new ServiceDefinitionAttribute();
                    sda.setCode(attributeCode);
                    sda.setAttributeOrder(serviceDefinitionAttribute.getAttributeOrder());
                    sda.setRequired(serviceDefinitionAttribute.isRequired());
                    sda.setVariable(serviceDefinitionAttribute.isVariable());
                    sda.setDatatype(serviceDefinitionAttribute.getDatatype());

                    List<AttributeValue> values = new ArrayList<>();

                    if (serviceDefinitionAttribute.getDatatype() == AttributeDataType.SINGLEVALUELIST ||
                            serviceDefinitionAttribute.getDatatype() == AttributeDataType.MULTIVALUELIST) {

                        List<AttributeValue> attributeValues = serviceDefinitionAttribute.getValues();
                        if (attributeValues != null) {
                            if (v instanceof ArrayList) {
                                ((ArrayList<?>) v).forEach(s -> {
                                    values.add(new AttributeValue((String) s, getAttributeValueName((String) s, attributeValues)));
                                });
                            } else {
                                values.add(new AttributeValue((String) v, getAttributeValueName((String) v, attributeValues)));
                            }
                        }
                    } else {
                        // we need a way to capture the user's response. We will do so by adding an attribute value where
                        // the key is the code and the value is the user's response.
                        values.add(new AttributeValue(attributeCode, (String) v));
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

    private String getAttributeValueName(String valueKey, List<AttributeValue> attributeValues) {
        String valueName = null;
        Optional<AttributeValue> attributeValueOptional = attributeValues.stream()
                .filter(attributeValue -> attributeValue.getKey().equals(valueKey)).findFirst();

        if (attributeValueOptional.isPresent()) {
            valueName = attributeValueOptional.get().getName();
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

    public Page<ServiceRequestDTO> findAll(GetServiceRequestsDTO requestDTO, String jurisdictionId) {
        return getServiceRequestPage(requestDTO, jurisdictionId).map(ServiceRequestService::convertToDTO);
    }

    private Page<ServiceRequest> getServiceRequestPage(GetServiceRequestsDTO requestDTO, String jurisdictionId) {
        String serviceRequestIds = requestDTO.getId();
        String serviceCode = requestDTO.getServiceCode();
        ServiceRequestStatus status = requestDTO.getStatus();
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

        return getJurisdictionServiceRequests(jurisdictionId, pageable, serviceCode, status, startDate, endDate);
    }

    private Page<ServiceRequest> getJurisdictionServiceRequests(String jurisdictionId, Pageable pageable, String serviceCode, ServiceRequestStatus status, Instant startDate, Instant endDate) {

        if (StringUtils.hasText(serviceCode) && status != null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBetween(jurisdictionId, serviceCode, status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedAfter(jurisdictionId, serviceCode, status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBefore(jurisdictionId, serviceCode, status, endDate, pageable);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatus(jurisdictionId, serviceCode, status, pageable);
        } else if (StringUtils.hasText(serviceCode) && status == null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(jurisdictionId, serviceCode, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(jurisdictionId, serviceCode, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(jurisdictionId, serviceCode, endDate, pageable);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCode(jurisdictionId, serviceCode, pageable);
        } else if (status != null && StringUtils.isEmpty(serviceCode)) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedBetween(jurisdictionId, status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedAfter(jurisdictionId, status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedBefore(jurisdictionId, status, endDate, pageable);
            }

            return serviceRequestRepository.findByJurisdictionIdAndStatus(jurisdictionId, status, pageable);
        }

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
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);

        return serviceRequestOptional.map(ServiceRequestService::convertToDTO).orElse(null);
    }

    public StreamedFile getAllServiceRequests(DownloadRequestsArgumentsDTO downloadRequestsArgumentsDTO, String jurisdictionId) throws MalformedURLException {

        List<DownloadServiceRequestDTO> downloadServiceRequestDTOS = getServiceRequests(downloadRequestsArgumentsDTO, jurisdictionId).stream()
                .map(serviceRequest -> {
                    DownloadServiceRequestDTO dto = new DownloadServiceRequestDTO(serviceRequest);

                    if (serviceRequest.getAttributesJson() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            ServiceDefinitionAttribute[] serviceDefinitionAttributes =
                                    objectMapper.readValue(serviceRequest.getAttributesJson(), ServiceDefinitionAttribute[].class);
                            List<String> values = Arrays.stream(serviceDefinitionAttributes)
                                    .flatMap(serviceDefinitionAttribute -> {
                                        if (serviceDefinitionAttribute.getValues() != null) {
                                            return serviceDefinitionAttribute.getValues().stream();
                                        }
                                        return Stream.of();
                                    })
                                    .map(AttributeValue::getKey).collect(Collectors.toList());

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
        ServiceRequestStatus status = requestDTO.getStatus();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();

        Optional<ServiceRequest> byServiceName;
        if (serviceName == null) {
            byServiceName = Optional.empty();
        } else {
            byServiceName = serviceRequestRepository.findByServiceServiceNameIlikeAndJurisdictionId(serviceName, jurisdictionId);
        }

        return getJurisdictionServiceRequests(jurisdictionId, byServiceName, status, startDate, endDate);
    }

    private List<ServiceRequest> getJurisdictionServiceRequests(String jurisdictionId, Optional<ServiceRequest> byServiceName, ServiceRequestStatus status, Instant startDate, Instant endDate) {
        String serviceCode;
        if (byServiceName.isPresent() && status != null) {
            serviceCode = byServiceName.get().getService().getServiceCode();
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBetween(jurisdictionId, serviceCode, status, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedAfter(jurisdictionId, serviceCode, status, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatusAndDateCreatedBefore(jurisdictionId, serviceCode, status, endDate);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndStatus(jurisdictionId, serviceCode, status);
        } else if (byServiceName.isPresent() && status == null) {
            serviceCode = byServiceName.get().getService().getServiceCode();
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBetween(jurisdictionId, serviceCode, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedAfter(jurisdictionId, serviceCode, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCodeAndDateCreatedBefore(jurisdictionId, serviceCode, endDate);
            }

            return serviceRequestRepository.findByJurisdictionIdAndServiceServiceCode(jurisdictionId, serviceCode);
        } else if (status != null && byServiceName.isEmpty()) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedBetween(jurisdictionId, status, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedAfter(jurisdictionId, status, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByJurisdictionIdAndStatusAndDateCreatedBefore(jurisdictionId, status, endDate);
            }

            return serviceRequestRepository.findByJurisdictionIdAndStatus(jurisdictionId, status);
        }

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
