// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.ServiceType;
import app.model.service.servicedefinition.AttributeDataType;
import app.model.service.servicedefinition.AttributeValue;
import app.model.service.servicedefinition.ServiceDefinition;
import app.model.service.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicerequest.ServiceRequest;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Requires(property = "app-data.bootstrap.data.enabled", value = StringUtils.TRUE)
@ConfigurationProperties("app-data.bootstrap")
public class Bootstrap {

    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    private Map<String, Object> data;

    private final ServiceRepository serviceRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public Bootstrap(ServiceRepository serviceRepository, JurisdictionRepository jurisdictionRepository) {
        this.serviceRepository = serviceRepository;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    @EventListener
    public void devData(ServerStartupEvent event) {
        if(data != null) {
            if(data.containsKey("jurisdictions")) {
                List<Map<String, ?>> jurisdictions = (List<Map<String, ?>>) data.get("jurisdictions");
                jurisdictions.forEach(juridictionsMap -> {
                    Jurisdiction jurisdiction = jurisdictionRepository.save(
                            new Jurisdiction((String) juridictionsMap.get("id"), (String) juridictionsMap.get("tenant")));

                    List<Map<String, ?>> services = (List<Map<String, ?>>) juridictionsMap.get("services");
                    processAndStoreServices(services, jurisdiction);
                });
            }
        }
    }

    private void processAndStoreServices(List<Map<String, ?>> services, Jurisdiction jurisdiction) {

        services.stream().forEach(svc -> {
            String serviceName = (String) svc.get("serviceName");
            Service service = new Service(serviceName);
            service.setServiceCode((String) svc.get("serviceCode"));
            service.setDescription((String) svc.get("description"));
            service.setType(ServiceType.valueOf(((String) svc.get("type")).toUpperCase()));
            service.setJurisdiction(jurisdiction);

            if (svc.containsKey("serviceDefinition")) {
                service.setMetadata(true);

                Map definitionMap = (Map) svc.get("serviceDefinition");

                ServiceDefinition serviceDefinition = new ServiceDefinition();
                serviceDefinition.setServiceCode((String) svc.get("serviceCode"));

                List<Map<String, Object>> attributes = (List<Map<String, Object>>) definitionMap.get("attributes");
                serviceDefinition.setAttributes(attributes.stream().map(stringObjectMap -> {
                    AttributeDataType attributeDataType = AttributeDataType.valueOf(((String) stringObjectMap.get(
                            "datatype")).toUpperCase());
                    String attributeCode = (String) stringObjectMap.get("code");

                    ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
                    serviceDefinitionAttribute.setVariable((Boolean) stringObjectMap.get("variable"));
                    serviceDefinitionAttribute.setCode(attributeCode);
                    serviceDefinitionAttribute.setDatatype(attributeDataType);
                    serviceDefinitionAttribute.setRequired((Boolean) stringObjectMap.get("required"));
                    serviceDefinitionAttribute.setDatatypeDescription((String) stringObjectMap.get("datatypeDescription"));
                    serviceDefinitionAttribute.setAttributeOrder((Integer) stringObjectMap.get("order"));
                    serviceDefinitionAttribute.setDescription((String) stringObjectMap.get("description"));

                    List<Map<String, String>> values = (List<Map<String, String>>) stringObjectMap.get("values");
                    if ((values == null || values.isEmpty()) && (attributeDataType.equals(AttributeDataType.MULTIVALUELIST) || attributeDataType.equals(AttributeDataType.SINGLEVALUELIST))) {
                        LOG.warn(String.format("Attribute with code %s does not contain values despite being a MULTIVALUELIST or SINGLEVALUELIST", attributeCode));
                    }
                    if (values != null) {
                        List<AttributeValue> attributeValues =
                                values.stream().map(stringStringMap -> new AttributeValue(stringStringMap.get("key"),
                                        stringStringMap.get("name"))).collect(Collectors.toList());
                        serviceDefinitionAttribute.setValues(attributeValues);
                    }
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
                    serviceRequest.setJurisdiction(jurisdiction);

                    savedService.addServiceRequest(serviceRequest);
                });
            }

            serviceRepository.update(savedService);
        });
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
