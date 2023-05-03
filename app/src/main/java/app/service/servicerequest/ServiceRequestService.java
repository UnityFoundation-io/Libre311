package app.service.servicerequest;

import app.dto.download.DownloadRequestsArgumentsDTO;
import app.dto.download.DownloadServiceRequestDTO;
import app.dto.servicerequest.*;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.servicedefinition.AttributeValue;
import app.model.service.servicedefinition.ServiceDefinition;
import app.model.service.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.recaptcha.ReCaptchaService;
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

import java.io.*;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Singleton
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRepository serviceRepository;
    private final ReCaptchaService reCaptchaService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository, ServiceRepository serviceRepository, ReCaptchaService reCaptchaService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.serviceRepository = serviceRepository;
        this.reCaptchaService = reCaptchaService;
    }

    public PostResponseServiceRequestDTO createServiceRequest(HttpRequest<?> request, PostRequestServiceRequestDTO serviceRequestDTO) {
        if (!reCaptchaService.verifyReCaptcha(serviceRequestDTO.getgRecaptchaResponse())) {
            return null;
        }

        Optional<Service> serviceByServiceCodeOptional = serviceRepository.findByServiceCode(serviceRequestDTO.getServiceCode());

        if (serviceByServiceCodeOptional.isEmpty()) {
            return null; // todo return 'corresponding service not found
        }

        // validate if a location is provided
        boolean latLongProvided = StringUtils.hasText(serviceRequestDTO.getLatitude()) &&
                StringUtils.hasText(serviceRequestDTO.getLongitude());

        if (!latLongProvided &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressString()) &&
                StringUtils.isEmpty(serviceRequestDTO.getAddressId())) {
            return null; // todo throw exception
        }

        // validate if additional attributes are required
        List<ServiceDefinitionAttribute> requestAttributes = null;
        Service service = serviceByServiceCodeOptional.get();
        if (service.isMetadata()) {
            // get service definition
            String serviceDefinitionJson = service.getServiceDefinitionJson();
            if (serviceDefinitionJson == null || serviceDefinitionJson.isBlank()) {
                // service definition does not exists despite service requiring it.
                return null; // should not be in this state and admin needs to be aware.
            }

            requestAttributes = buildAttributesFromRequest(request, service);
            if (requestAttributes.isEmpty()) {
                return null; // todo throw exception - must provide attributes
            }
            if (!requestAttributesHasAllRequiredServiceDefinitionAttributes(serviceDefinitionJson, requestAttributes)) {
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

    private List<ServiceDefinitionAttribute> buildAttributesFromRequest(HttpRequest<?> request, Service service) {
        Optional<Map> body = request.getBody(Map.class);

        List<ServiceDefinitionAttribute> attributes = new ArrayList<>();
        if (body.isPresent()) {
            Map<String, Object> map = body.get();
            map.forEach((k, v) -> {
                if (k.startsWith("attribute[")) {
                    String code = k.substring(k.indexOf("[") + 1, k.indexOf("]"));

                    ServiceDefinitionAttribute sda = new ServiceDefinitionAttribute();
                    sda.setCode(code);
                    List<AttributeValue> values = new ArrayList<>();
                    if (v instanceof ArrayList) {
                        ((ArrayList<?>) v).forEach(s -> {
                            values.add(new AttributeValue((String) s, getAttributeValueName(code, (String) s, service)));
                        });
                    } else {
                        values.add(new AttributeValue((String) v, getAttributeValueName(code, (String) v, service)));
                    }
                    sda.setValues(values);

                    attributes.add(sda);
                }
            });
        }

        return attributes;
    }

    private String getAttributeValueName(String code, String valueKey, Service service) {
        ObjectMapper objectMapper = new ObjectMapper();
        String valueName = null;
        try {
            String serviceDefinitionJson = service.getServiceDefinitionJson();
            if (serviceDefinitionJson != null) {
                ServiceDefinition serviceDefinition = objectMapper.readValue(serviceDefinitionJson, ServiceDefinition.class);
                Optional<AttributeValue> attributeValueOptional = serviceDefinition.getAttributes().stream()
                        .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getCode().equals(code))
                        .flatMap((Function<ServiceDefinitionAttribute, Stream<AttributeValue>>) serviceDefinitionAttribute -> serviceDefinitionAttribute.getValues().stream())
                        .filter(attributeValue -> attributeValue.getKey().equals(valueKey)).findFirst();

                if (attributeValueOptional.isPresent()) {
                    valueName = attributeValueOptional.get().getName();
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return valueName;
    }

    private ServiceRequest transformDtoToServiceRequest(PostRequestServiceRequestDTO serviceRequestDTO, Service service) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(service);
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

    public Page<ServiceRequestDTO> findAll(GetServiceRequestsDTO requestDTO) {
        return getServiceRequestPage(requestDTO).map(serviceRequest -> {
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
        });
    }

    private Page<ServiceRequest> getServiceRequestPage(GetServiceRequestsDTO requestDTO) {
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
            List<String> requestIds = Arrays.stream(serviceRequestIds.split(",")).map(String::trim).collect(Collectors.toList());
            return serviceRequestRepository.findByIdIn(requestIds, pageable);
        }

        if (StringUtils.hasText(serviceCode) && status != null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBetween(serviceCode, status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedAfter(serviceCode, status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBefore(serviceCode, status, endDate, pageable);
            }

            return serviceRequestRepository.findByServiceServiceCodeAndStatus(serviceCode, status, pageable);
        } else if (StringUtils.hasText(serviceCode) && status == null) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBetween(serviceCode, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedAfter(serviceCode, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBefore(serviceCode, endDate, pageable);
            }

            return serviceRequestRepository.findByServiceServiceCode(serviceCode, pageable);
        } else if (status != null && StringUtils.isEmpty(serviceCode)) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBetween(status, startDate, endDate, pageable);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByStatusAndDateCreatedAfter(status, startDate, pageable);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBefore(status, endDate, pageable);
            }

            return serviceRequestRepository.findByStatus(status, pageable);
        }


        if (startDate != null && endDate != null) {
            return serviceRequestRepository.findByDateCreatedBetween(startDate, endDate, pageable);
        } else if (startDate != null && endDate == null) {
            // just start
            return serviceRequestRepository.findByDateCreatedAfter(startDate, pageable);
        } else if (startDate == null && endDate != null) {
            // just end
            return serviceRequestRepository.findByDateCreatedBefore(endDate, pageable);
        }

        return serviceRequestRepository.findAll(pageable);
    }

    public ServiceRequestDTO getServiceRequest(String serviceRequestId) {
        Optional<ServiceRequest> byId = serviceRequestRepository.findById(serviceRequestId);
        return byId.map(serviceRequest -> {
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
        }).orElse(null);
    }

    public StreamedFile  getAllServiceRequests(DownloadRequestsArgumentsDTO downloadRequestsArgumentsDTO) throws MalformedURLException {

        List<DownloadServiceRequestDTO> downloadServiceRequestDTOS = getServiceRequests(downloadRequestsArgumentsDTO).stream()
                .map(serviceRequest -> {
                    DownloadServiceRequestDTO dto = new DownloadServiceRequestDTO(serviceRequest);

                    if (serviceRequest.getAttributesJson() != null) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            ServiceDefinitionAttribute[] serviceDefinitionAttributes =
                                    objectMapper.readValue(serviceRequest.getAttributesJson(), ServiceDefinitionAttribute[].class);
                            List<String> values = Arrays.stream(serviceDefinitionAttributes)
                                    .flatMap(serviceDefinitionAttribute -> serviceDefinitionAttribute.getValues().stream())
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

    private List<ServiceRequest> getServiceRequests(DownloadRequestsArgumentsDTO requestDTO) {
        String serviceName = requestDTO.getServiceName();
        ServiceRequestStatus status = requestDTO.getStatus();
        Instant startDate = requestDTO.getStartDate();
        Instant endDate = requestDTO.getEndDate();

        Optional<ServiceRequest> byServiceName;
        if (serviceName == null) {
            byServiceName = Optional.empty();
        } else {
            byServiceName = serviceRequestRepository.findByServiceServiceNameIlike(serviceName);
        }

        String serviceCode;
        if (byServiceName.isPresent() && status != null) {
            serviceCode = byServiceName.get().getService().getServiceCode();
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBetween(serviceCode, status, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedAfter(serviceCode, status, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndStatusAndDateCreatedBefore(serviceCode, status, endDate);
            }

            return serviceRequestRepository.findByServiceServiceCodeAndStatus(serviceCode, status);
        } else if (byServiceName.isPresent() && status == null) {
            serviceCode = byServiceName.get().getService().getServiceCode();
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBetween(serviceCode, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedAfter(serviceCode, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByServiceServiceCodeAndDateCreatedBefore(serviceCode, endDate);
            }

            return serviceRequestRepository.findByServiceServiceCode(serviceCode);
        } else if (status != null && byServiceName.isEmpty()) {
            if (startDate != null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBetween(status, startDate, endDate);
            } else if (startDate != null && endDate == null) {
                return serviceRequestRepository.findByStatusAndDateCreatedAfter(status, startDate);
            } else if (startDate == null && endDate != null) {
                return serviceRequestRepository.findByStatusAndDateCreatedBefore(status, endDate);
            }

            return serviceRequestRepository.findByStatus(status);
        }

        if (startDate != null && endDate != null) {
            return serviceRequestRepository.findByDateCreatedBetween(startDate, endDate);
        } else if (startDate != null && endDate == null) {
            // just start
            return serviceRequestRepository.findByDateCreatedAfter(startDate);
        } else if (startDate == null && endDate != null) {
            // just end
            return serviceRequestRepository.findByDateCreatedBefore(endDate);
        }

        return (List<ServiceRequest>) serviceRequestRepository.findAll();
    }
}
