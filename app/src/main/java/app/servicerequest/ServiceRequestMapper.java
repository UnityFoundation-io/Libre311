package app.servicerequest;

import app.servicedefinition.AttributeValueDTO;
import app.servicedefinition.ServiceDefinitionAttributeDTO;
import app.servicedefinition.AttributeDataType;
import app.servicedefinition.ServiceDefinition;
import app.servicedefinition.AttributeValue;
import app.servicedefinition.ServiceDefinitionAttribute;
import app.geometry.LibreGeometryFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

@Singleton
public class ServiceRequestMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestMapper.class);
    private final ObjectMapper objectMapper;
    private final LibreGeometryFactory libreGeometryFactory;

    public ServiceRequestMapper(ObjectMapper objectMapper, LibreGeometryFactory libreGeometryFactory) {
        this.objectMapper = objectMapper;
        this.libreGeometryFactory = libreGeometryFactory;
    }

    ServiceRequestDTO toDto(ServiceRequest serviceRequest) {
        ServiceRequestDTO serviceRequestDTO = new ServiceRequestDTO(serviceRequest);

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

    SensitiveServiceRequestDTO toSensitiveDto(ServiceRequest serviceRequest) {
        SensitiveServiceRequestDTO serviceRequestDTO = new SensitiveServiceRequestDTO(serviceRequest);

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

    ServiceRequest toServiceRequest(ServiceRequestPostRequest serviceRequestDTO, ServiceDefinition serviceDefinition) {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setService(serviceDefinition);
        serviceRequest.setJurisdiction(serviceDefinition.getJurisdiction());
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

    public List<ServiceDefinitionAttributeDTO> mapAttributeDtos(Map<String, String> dtoAttributes, List<ServiceDefinitionAttribute> serviceDefinitionAttributes) {

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
                        throw new ServiceRequestService.InvalidServiceRequestException("Code should be an Integer.");
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

    void applyPatch(ServiceRequestUpdateRequest serviceRequestDTO, ServiceRequest serviceRequest) {
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
}
