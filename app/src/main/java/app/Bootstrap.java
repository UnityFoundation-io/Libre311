package app;

import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.ServiceType;
import app.model.service.servicedefinition.*;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Requires(property = "wemove.bootstrap.data.enabled", value = StringUtils.TRUE)
@ConfigurationProperties("wemove.bootstrap")
public class Bootstrap {

    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    private Map<String, Object> data;

    private final ServiceRepository serviceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public Bootstrap(ServiceRepository serviceRepository, ServiceRequestRepository serviceRequestRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @EventListener
    public void devData(ServerStartupEvent event) {
        if(data != null) {

            if(data.containsKey("services")) {
                ((List<Map<String, ?>>) data.get("services")).stream().forEach(svc -> {
                    String serviceName = (String) svc.get("serviceName");
                    Service service = new Service(serviceName);
                    service.setServiceCode((String) svc.get("serviceCode"));
                    service.setDescription((String) svc.get("description"));
                    service.setMetadata((boolean) svc.get("metadata"));
                    service.setType(ServiceType.valueOf(((String) svc.get("type")).toUpperCase()));

                    if (svc.containsKey("serviceDefinition")) {
                        Map definitionMap = (Map) svc.get("serviceDefinition");

                        ServiceDefinition serviceDefinition = new ServiceDefinition();
                        serviceDefinition.setServiceCode((String) definitionMap.get("serviceCode"));

                        List<Map<String, Object>> attributes = (List<Map<String, Object>>) definitionMap.get("attributes");
                        serviceDefinition.setAttributes(attributes.stream().map(stringObjectMap -> {
                            ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
                            serviceDefinitionAttribute.setVariable((Boolean) stringObjectMap.get("variable"));
                            serviceDefinitionAttribute.setCode((String) stringObjectMap.get("code"));
                            serviceDefinitionAttribute.setDatatype(AttributeDataType.valueOf(((String) stringObjectMap.get("datatype")).toUpperCase()));
                            serviceDefinitionAttribute.setRequired((Boolean) stringObjectMap.get("required"));
                            serviceDefinitionAttribute.setDatatypeDescription((String) stringObjectMap.get("datatypeDescription"));
                            serviceDefinitionAttribute.setAttributeOrder((Integer) stringObjectMap.get("order"));
                            serviceDefinitionAttribute.setDescription((String) stringObjectMap.get("description"));

                            List<Map<String, String>> values = (List<Map<String, String>>) stringObjectMap.get("values");
                            List<AttributeValue> attributeValues = values.stream()
                                    .map(stringStringMap -> new AttributeValue(stringStringMap.get("key"), stringStringMap.get("name")))
                                    .collect(Collectors.toList());
                            serviceDefinitionAttribute.setValues(attributeValues);
                            return serviceDefinitionAttribute;
                        }).collect(Collectors.toList()));

                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            service.setServiceDefinitionJson(objectMapper.writeValueAsString(serviceDefinition));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    Service savedService = serviceRepository.save(service);

                    if (svc.containsKey("serviceRequests")) {
                        List<Map> serviceRequests = (List<Map>) svc.get("serviceRequests");

                        serviceRequests.forEach(svcReq -> {
                            ServiceRequest serviceRequest = new ServiceRequest();
                            serviceRequest.setDescription((String) svcReq.get("description"));
                            serviceRequest.setAddressString((String) svcReq.get("address"));
                            serviceRequest.setLatitude((String) svcReq.get("lat"));
                            serviceRequest.setLongitude((String) svcReq.get("long"));
                            serviceRequest.setStatus(ServiceRequestStatus.valueOf(((String) svcReq.get("status")).toUpperCase()));
                            serviceRequest.setZipCode((String) svcReq.get("zipcode"));
                            serviceRequest.setAgencyResponsible((String) svcReq.get("agency_responsible"));
                            serviceRequest.setAgencyResponsible((String) svcReq.get("agency_responsible"));
                            serviceRequest.setMediaUrl((String) svcReq.get("media_url"));
                            serviceRequest.setAddressId((String) svcReq.get("address_id"));
                            serviceRequest.setServiceNotice((String) svcReq.get("service_notice"));
                            serviceRequest.setStatusNotes((String) svcReq.get("status_notes"));
                            serviceRequest.setService(savedService);

                            savedService.addServiceRequest(serviceRequest);
                        });
                    }

                    serviceRepository.update(savedService);
                });
            }
        }
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
