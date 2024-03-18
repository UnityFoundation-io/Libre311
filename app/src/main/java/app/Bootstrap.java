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
import app.model.service.AttributeDataType;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.ServiceType;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicedefinition.*;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestStatus;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Requires(property = "app-data.bootstrap.data.enabled", value = StringUtils.TRUE)
@ConfigurationProperties("app-data.bootstrap")
public class Bootstrap {

    @MapFormat(transformation = MapFormat.MapTransformation.NESTED)
    private Map<String, Object> data;

    private final ServiceRepository serviceRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final ServiceGroupRepository serviceGroupRepository;
    private final ServiceDefinitionAttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);

    public Bootstrap(ServiceRepository serviceRepository, JurisdictionRepository jurisdictionRepository, ServiceGroupRepository serviceGroupRepository, ServiceDefinitionAttributeRepository attributeRepository, AttributeValueRepository attributeValueRepository) {
        this.serviceRepository = serviceRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.serviceGroupRepository = serviceGroupRepository;
        this.attributeRepository = attributeRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    @EventListener
    public void devData(ServerStartupEvent event) {
        if(data != null) {

            if(data.containsKey("jurisdictions")) {
                List<Map<String, ?>> jurisdictions = (List<Map<String, ?>>) data.get("jurisdictions");
                jurisdictions.forEach(juridictionsMap -> {
                    Jurisdiction jurisdiction = jurisdictionRepository.save(
                            new Jurisdiction((String) juridictionsMap.get("id"), ((Integer) juridictionsMap.get("tenant")).longValue()));

                    List<String> groups = (List<String>) juridictionsMap.get("groups");
                    List<ServiceGroup> serviceGroups = processAndStoreGroups(groups, jurisdiction);

                    List<Map<String, ?>> services = (List<Map<String, ?>>) juridictionsMap.get("services");
                    processAndStoreServices(services, jurisdiction, serviceGroups);
                });
            }
        }
    }

    private List<ServiceGroup> processAndStoreGroups(List<String> groups, Jurisdiction jurisdiction) {
        return groups.stream().map(name -> serviceGroupRepository.save(new ServiceGroup(name, jurisdiction))).collect(Collectors.toList());
    }

    private void processAndStoreServices(List<Map<String, ?>> services, Jurisdiction jurisdiction, List<ServiceGroup> serviceGroups) {

        services.forEach(svc -> {
            String serviceName = (String) svc.get("serviceName");
            Service service = new Service(serviceName);
            service.setServiceCode((String) svc.get("serviceCode"));
            service.setDescription((String) svc.get("description"));
            service.setType(ServiceType.valueOf(((String) svc.get("type")).toUpperCase()));
            service.setJurisdiction(jurisdiction);
            String groupStr = (String) svc.get("group");
            Optional<ServiceGroup> group = serviceGroups.stream()
                    .filter(serviceGroup -> groupStr.equals(serviceGroup.getName()))
                    .findFirst();
            service.setServiceGroup(group.get());

            Service savedService = serviceRepository.save(service);

            if (svc.containsKey("serviceDefinition")) {
                Map definitionMap = (Map) svc.get("serviceDefinition");

                List<Map<String, Object>> attributes = (List<Map<String, Object>>) definitionMap.get("attributes");
                attributes.forEach(stringObjectMap -> {
                    AttributeDataType attributeDataType = AttributeDataType.valueOf(((String) stringObjectMap.get("datatype")).toUpperCase());
                    String attributeCode = (String) stringObjectMap.get("code");

                    ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
                    serviceDefinitionAttribute.setService(savedService);
                    serviceDefinitionAttribute.setVariable((Boolean) stringObjectMap.get("variable"));
                    serviceDefinitionAttribute.setCode(attributeCode);
                    serviceDefinitionAttribute.setRequired((Boolean) stringObjectMap.get("required"));
                    serviceDefinitionAttribute.setDatatypeDescription((String) stringObjectMap.get("datatypeDescription"));
                    serviceDefinitionAttribute.setAttributeOrder((Integer) stringObjectMap.get("order"));
                    serviceDefinitionAttribute.setDescription((String) stringObjectMap.get("description"));
                    serviceDefinitionAttribute.setDatatype(attributeDataType);

                    ServiceDefinitionAttribute savedSDA = attributeRepository.save(serviceDefinitionAttribute);

                    List<Map<String, String>> values = (List<Map<String, String>>) stringObjectMap.get("values");
                    if ((values == null || values.isEmpty()) && (attributeDataType.equals(AttributeDataType.MULTIVALUELIST) || attributeDataType.equals(AttributeDataType.SINGLEVALUELIST))) {
                        LOG.warn(String.format("Attribute with code %s does not contain values despite being a MULTIVALUELIST or SINGLEVALUELIST", attributeCode));
                    }

                    if (values != null) {
                        values.forEach(stringStringMap -> attributeValueRepository.save(new AttributeValue(savedSDA, stringStringMap.get("name"))));
                    }
                });
            }
        });
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
