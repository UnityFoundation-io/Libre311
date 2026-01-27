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

import app.dto.download.CsvHeaders;
import app.dto.download.DownloadServiceRequestDTO;
import app.dto.servicedefinition.AttributeValueDTO;
import app.dto.servicedefinition.ServiceDefinitionAttributeDTO;
import app.dto.servicerequest.*;
import app.model.service.AttributeDataType;
import app.exception.Libre311BaseException;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.servicedefinition.AttributeValue;
import app.model.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicedefinition.ServiceDefinitionAttributeRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRemovalSuggestion;
import app.model.servicerequest.ServiceRequestRemovalSuggestionCount;
import app.model.servicerequest.ServiceRequestRemovalSuggestionRepository;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.recaptcha.ReCaptchaService;
import app.security.Permission;
import app.security.UnityAuthService;
import app.service.geometry.LibreGeometryFactory;
import app.service.jurisdiction.JurisdictionBoundaryService;
import app.service.storage.StorageUrlUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.types.files.StreamedFile;
import jakarta.inject.Singleton;

import java.util.function.Function;
import jakarta.annotation.Nullable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
public class ServiceRequestService {

    static class ServiceRequestOutOfBoundsException extends Libre311BaseException {
        public ServiceRequestOutOfBoundsException() {
            super("The service request is out of bounds", HttpStatus.BAD_REQUEST);
        }
    }

    static class InvalidServiceRequestException extends Libre311BaseException {
        public InvalidServiceRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestService.class);
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestRemovalSuggestionRepository removalSuggestionRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceDefinitionAttributeRepository attributeRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final ReCaptchaService reCaptchaService;
    private final StorageUrlUtil storageUrlUtil;
    private final UnityAuthService unityAuthService;
    JurisdictionBoundaryService jurisdictionBoundaryService;
    LibreGeometryFactory libreGeometryFactory;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository,
        ServiceRequestRemovalSuggestionRepository removalSuggestionRepository,
        ServiceRepository serviceRepository,
        ServiceDefinitionAttributeRepository attributeRepository,
        JurisdictionRepository jurisdictionRepository,
        ReCaptchaService reCaptchaService, StorageUrlUtil storageUrlUtil,
        UnityAuthService unityAuthService,
        JurisdictionBoundaryService jurisdictionBoundaryService,
        LibreGeometryFactory libreGeometryFactory) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.removalSuggestionRepository = removalSuggestionRepository;
        this.serviceRepository = serviceRepository;
        this.attributeRepository = attributeRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.reCaptchaService = reCaptchaService;
        this.storageUrlUtil = storageUrlUtil;
        this.unityAuthService = unityAuthService;
        this.jurisdictionBoundaryService = jurisdictionBoundaryService;
        this.libreGeometryFactory = libreGeometryFactory;
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
        LOG.debug(serviceRequestDTO.toString());
        double lat = Double.parseDouble(serviceRequestDTO.getLatitude());
        double lng = Double.parseDouble(serviceRequestDTO.getLongitude());
        if (!jurisdictionBoundaryService.existsInJurisdiction(jurisdictionId, lat, lng)){
            throw new ServiceRequestOutOfBoundsException();
        }

        if (!validMediaUrl(serviceRequestDTO.getMediaUrl())) {
            throw new InvalidServiceRequestException("Media URL is invalid.");
        }

        Optional<Service> serviceByServiceCodeOptional = serviceRepository.findById(serviceRequestDTO.getServiceCode());

        if (serviceByServiceCodeOptional.isEmpty()) {
            throw new InvalidServiceRequestException("Corresponding service is not found.");
        }

        if (!jurisdictionId.equals(serviceByServiceCodeOptional.get().getJurisdiction().getId())) {
            throw new InvalidServiceRequestException(
                "Mismatch between jurisdiction_id provided and Service's associated jurisdiction.");
        }

        // validate if additional attributes are required
        Service service = serviceByServiceCodeOptional.get();
        ServiceRequest serviceRequest = transformDtoToServiceRequest(serviceRequestDTO, service);
        List<ServiceDefinitionAttribute> serviceDefinitionAttributes = attributeRepository.findAllByServiceId(service.getId());
        if (!serviceDefinitionAttributes.isEmpty()) {
            List<ServiceDefinitionAttributeDTO> requestAttributes = buildUserResponseAttributesFromRequest(serviceRequestDTO.getAttributes(), serviceDefinitionAttributes);
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

    public void createRemovalSuggestion(Long serviceRequestId, String jurisdictionId, PostRequestServiceRequestRemovalSuggestionDTO suggestionDTO) {
        reCaptchaService.verifyReCaptcha(suggestionDTO.getgRecaptchaResponse());
        Optional<ServiceRequest> serviceRequestOptional = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);
        if (serviceRequestOptional.isEmpty()) {
            throw new InvalidServiceRequestException("Service Request not found.");
        }

        ServiceRequestRemovalSuggestion suggestion = new ServiceRequestRemovalSuggestion();
        suggestion.setServiceRequestId(serviceRequestOptional.get().getId());
        suggestion.setJurisdictionId(jurisdictionId);
        suggestion.setEmail(suggestionDTO.getEmail());
        suggestion.setName(suggestionDTO.getName());
        suggestion.setPhone(suggestionDTO.getPhone());
        suggestion.setReason(suggestionDTO.getReason());

        removalSuggestionRepository.save(suggestion);
    }

    public List<ServiceRequestRemovalSuggestionDTO> getRemovalSuggestions(String jurisdictionId, Long serviceRequestId) {
        return removalSuggestionRepository.findAllByJurisdictionIdAndServiceRequestId(jurisdictionId, serviceRequestId)
                .stream().map(this::convertToSuggestionDTO).toList();
    }

    private ServiceRequestRemovalSuggestionDTO convertToSuggestionDTO(ServiceRequestRemovalSuggestion suggestion) {
        ServiceRequestRemovalSuggestionDTO dto = new ServiceRequestRemovalSuggestionDTO();
        dto.setId(suggestion.getId());
        dto.setServiceRequestId(suggestion.getServiceRequestId());
        dto.setEmail(suggestion.getEmail());
        dto.setName(suggestion.getName());
        dto.setPhone(suggestion.getPhone());
        dto.setReason(suggestion.getReason());
        dto.setDateCreated(suggestion.getDateCreated());
        return dto;
    }

    private boolean validMediaUrl(String mediaUrl) {
        if (mediaUrl == null) return true;
        return mediaUrl.startsWith(storageUrlUtil.getBucketUrlString());
    }

    private boolean requestAttributesHasAllRequiredServiceDefinitionAttributes(List<ServiceDefinitionAttribute> serviceDefinitionAttributes, List<ServiceDefinitionAttributeDTO> requestAttributes) {
        // collect all required attributes
        List<Long> requiredCodes = serviceDefinitionAttributes.stream()
                .filter(ServiceDefinitionAttribute::isRequired)
                .map(ServiceDefinitionAttribute::getId)
                .collect(Collectors.toList());

        // for each attr, check if it exists in requestAttributes
        List<Long> requestCodes = requestAttributes.stream()
                .map(ServiceDefinitionAttributeDTO::getId)
                .collect(Collectors.toList());
        LOG.debug("Required attributes: {}", requiredCodes);
        LOG.debug("Request attributes: {}", requestCodes);
        return requestCodes.containsAll(requiredCodes);
    }

    private List<ServiceDefinitionAttributeDTO> buildUserResponseAttributesFromRequest(Map<String, String> dtoAttributes, List<ServiceDefinitionAttribute> serviceDefinitionAttributes) {

        Optional<Map<String, String>> body = Optional.ofNullable(dtoAttributes);
        LOG.debug("Request body: {}", body);
        List<ServiceDefinitionAttributeDTO> attributes = new ArrayList<>();
        if (body.isPresent()) {
            body.get().forEach((k, v) -> {
                if (v != null && v.trim().isEmpty()) {
                    return;
                }
                if (k.startsWith("attribute[")) {
                    String attributeCodeStr = k.substring(k.indexOf("[") + 1, k.indexOf("]"));
                    Long attributeCode;
                    try {
                        attributeCode = Long.parseLong(attributeCodeStr);
                    } catch (NumberFormatException nfe) {
                        throw new InvalidServiceRequestException("Code should be an Integer.");
                    }

                    // search for attribute by code in serviceDefinitionAttributes
                    Optional<ServiceDefinitionAttribute> serviceDefinitionAttributeOptional = serviceDefinitionAttributes.stream()
                            .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getId().equals(attributeCode))
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
                    sda.setId(attributeCode);
                    sda.setAttributeOrder(serviceDefinitionAttribute.getAttributeOrder());
                    sda.setRequired(serviceDefinitionAttribute.isRequired());
                    sda.setVariable(serviceDefinitionAttribute.isVariable());
                    sda.setDatatype(serviceDefinitionAttribute.getDatatype());
                    sda.setDescription(serviceDefinitionAttribute.getDescription());

                    List<AttributeValueDTO> values = new ArrayList<>();

                    if (serviceDefinitionAttribute.getDatatype() == AttributeDataType.SINGLEVALUELIST ||
                            serviceDefinitionAttribute.getDatatype() == AttributeDataType.MULTIVALUELIST) {

                        Set<AttributeValue> attributeValues = serviceDefinitionAttribute.getAttributeValues();
                        if (attributeValues != null) {
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
                        values.add(new AttributeValueDTO(String.valueOf(attributeCode), v));
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
        serviceRequest.setLocation(libreGeometryFactory.createPoint(serviceRequestDTO.getLatitude(),
            serviceRequestDTO.getLongitude()));
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

    static void applyPatch(PatchServiceRequestDTO serviceRequestDTO, ServiceRequest serviceRequest) {
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
            serviceRequest.setExpectedDate(serviceRequestDTO.getExpected_date().orElse(null));
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

        // Get the visibility days from jurisdiction config
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionId(jurisdictionId);
        int closedRequestDaysVisible = canViewSensitive
                ? jurisdiction.getClosedRequestDaysVisibleAdmin()
                : jurisdiction.getClosedRequestDaysVisibleUser();

        Page<ServiceRequest> page = getServiceRequestPage(requestDTO, jurisdictionId, closedRequestDaysVisible);
        Page<ServiceRequestDTO> dtoPage = page.map(mapper);

        if (canViewSensitive && !dtoPage.getContent().isEmpty()) {
            List<Long> ids = dtoPage.getContent().stream().map(ServiceRequestDTO::getId).collect(Collectors.toList());
            List<ServiceRequestRemovalSuggestionCount> counts = removalSuggestionRepository.countByServiceRequestIdIn(ids);
            Map<Long, Long> countMap = counts.stream().collect(Collectors.toMap(ServiceRequestRemovalSuggestionCount::getServiceRequestId, ServiceRequestRemovalSuggestionCount::getCount));

            dtoPage.getContent().forEach(dto -> {
                dto.setRemovalSuggestionsCount(countMap.getOrDefault(dto.getId(), 0L));
            });
        }

        return dtoPage;
    }

    private Page<ServiceRequest> getServiceRequestPage(GetServiceRequestsDTO requestDTO, String jurisdictionId, int closedRequestDaysVisible) {
        String serviceRequestIds = requestDTO.getId();
        List<Long> serviceCodes = requestDTO.getServiceCodes();
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

        // Calculate the cutoff date for closed requests visibility
        Instant closedRequestCutoffDate = Instant.now().minus(closedRequestDaysVisible, ChronoUnit.DAYS);

        return serviceRequestRepository.findAllBy(jurisdictionId, serviceCodes, statuses, priorities, startDate, endDate, closedRequestCutoffDate, pageable);
    }

    public ServiceRequestDTO getServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return findServiceRequest(serviceRequestId, jurisdictionId)
                .map(ServiceRequestService::convertToDTO)
                .orElse(null);
    }

    private Optional<ServiceRequest> findServiceRequest(Long serviceRequestId, String jurisdictionId) {
        return serviceRequestRepository.findByIdAndJurisdictionId(serviceRequestId, jurisdictionId);
    }

    public StreamedFile getAllServiceRequests(GetServiceRequestsDTO requestDTO, String jurisdictionId) throws MalformedURLException {
        // CSV export should not filter closed requests - export all data
        List<DownloadServiceRequestDTO> downloadServiceRequestDTOS = getServiceRequestsUnfiltered(requestDTO, jurisdictionId).stream()
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
            tmpFile = File.createTempFile(now.toString().replaceAll(":", ""), ".csv");
            try (Writer writer  = new FileWriter(tmpFile)) {

                CSVFormat.Builder builder = CSVFormat.Builder.create(CSVFormat.DEFAULT);
                builder.setHeader(CsvHeaders.class);

                try (CSVPrinter csvPrinter = new CSVPrinter(writer, builder.build())) {
                    downloadServiceRequestDTOS.forEach(downloadServiceRequestDTO -> {
                        try {
                            csvPrinter.printRecord(
                                    downloadServiceRequestDTO.getJurisdictionId(),
                                    downloadServiceRequestDTO.getServiceName(),
                                    downloadServiceRequestDTO.getGroup(),
                                    downloadServiceRequestDTO.getServiceCode(),
                                    downloadServiceRequestDTO.getId(),
                                    downloadServiceRequestDTO.getServiceSubtype(),
                                    downloadServiceRequestDTO.getDescription(),
                                    downloadServiceRequestDTO.getMediaUrl(),
                                    downloadServiceRequestDTO.getAddress(),
                                    downloadServiceRequestDTO.getZipcode(),
                                    downloadServiceRequestDTO.getLatitude(),
                                    downloadServiceRequestDTO.getLongitude(),
                                    downloadServiceRequestDTO.getFirstName(),
                                    downloadServiceRequestDTO.getLastName(),
                                    downloadServiceRequestDTO.getEmail(),
                                    downloadServiceRequestDTO.getPhone(),
                                    downloadServiceRequestDTO.getDateCreated(),
                                    downloadServiceRequestDTO.getDateUpdated(),
                                    downloadServiceRequestDTO.getClosedDate(),
                                    downloadServiceRequestDTO.getAgencyResponsible(),
                                    downloadServiceRequestDTO.getAgencyEmail(),
                                    downloadServiceRequestDTO.getPriority(),
                                    downloadServiceRequestDTO.getStatus(),
                                    downloadServiceRequestDTO.getStatusNotes(),
                                    downloadServiceRequestDTO.getServiceNotice()
                            );
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new StreamedFile(tmpFile.toURI().toURL()).attach(now + ".csv");
    }

    private List<ServiceRequest> getServiceRequests(GetServiceRequestsDTO requestDTO, String jurisdictionId, int closedRequestDaysVisible) {
        String serviceRequestIds = requestDTO.getId();
        List<Long> serviceCodes = requestDTO.getServiceCodes();
        List<ServiceRequestStatus> statuses = requestDTO.getStatuses();
        List<ServiceRequestPriority> priorities = requestDTO.getPriorities();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();
        Pageable pageable = requestDTO.getPageable();

        Sort sort;
        if(pageable != null && pageable.isSorted()) {
            sort = pageable.getSort();
        } else {
            sort = Sort.of(new Sort.Order("dateCreated", Sort.Order.Direction.DESC, false));
        }

        if (StringUtils.hasText(serviceRequestIds)) {
            List<Long> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
            return serviceRequestRepository.findByIdInAndJurisdictionId(requestIds, jurisdictionId, sort);
        }

        // Calculate the cutoff date for closed requests visibility
        Instant closedRequestCutoffDate = Instant.now().minus(closedRequestDaysVisible, ChronoUnit.DAYS);

        return serviceRequestRepository.findAllBy(jurisdictionId, serviceCodes, statuses, priorities, startDate, endDate, closedRequestCutoffDate, sort);
    }

    private List<ServiceRequest> getServiceRequestsUnfiltered(GetServiceRequestsDTO requestDTO, String jurisdictionId) {
        String serviceRequestIds = requestDTO.getId();
        List<Long> serviceCodes = requestDTO.getServiceCodes();
        List<ServiceRequestStatus> statuses = requestDTO.getStatuses();
        List<ServiceRequestPriority> priorities = requestDTO.getPriorities();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();
        Pageable pageable = requestDTO.getPageable();

        Sort sort;
        if(pageable != null && pageable.isSorted()) {
            sort = pageable.getSort();
        } else {
            sort = Sort.of(new Sort.Order("dateCreated", Sort.Order.Direction.DESC, false));
        }

        if (StringUtils.hasText(serviceRequestIds)) {
            List<Long> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
            return serviceRequestRepository.findByIdInAndJurisdictionId(requestIds, jurisdictionId, sort);
        }

        // No closed request filtering for CSV export
        return serviceRequestRepository.findAllBy(jurisdictionId, serviceCodes, statuses, priorities, startDate, endDate, sort);
    }

    public int delete(Long serviceRequestId, String jurisdictionId) {
        removalSuggestionRepository.deleteByServiceRequestIdAndJurisdictionId(serviceRequestId, jurisdictionId);
        return serviceRequestRepository.delete(serviceRequestId, jurisdictionId);
    }

    public void deleteRemovalSuggestion(Long id, String jurisdictionId) {
        Optional<ServiceRequestRemovalSuggestion> suggestionOptional = removalSuggestionRepository.findById(id);
        if (suggestionOptional.isPresent()) {
            ServiceRequestRemovalSuggestion suggestion = suggestionOptional.get();
            if (suggestion.getJurisdictionId().equals(jurisdictionId)) {
                removalSuggestionRepository.softDelete(id, jurisdictionId);
                return;
            }
        }
        throw new InvalidServiceRequestException("Removal suggestion not found.");
    }
}
