package app.dto.servicedefinition;

import app.model.servicedefinition.ServiceDefinition;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;
import java.util.stream.Collectors;

@Introspected
public class ServiceDefinitionDTO {

    @JsonProperty("service_code")
    private String serviceCode;

    private List<ServiceDefinitionAttributeDTO> attributes;

    public ServiceDefinitionDTO(ServiceDefinition serviceDefinition) {
        this.serviceCode = serviceDefinition.getId();
        if (serviceDefinition.getAttributes() != null) {
            this.attributes = serviceDefinition.getAttributes()
                    .stream().map(ServiceDefinitionAttributeDTO::new)
                    .collect(Collectors.toList());
        }
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public List<ServiceDefinitionAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ServiceDefinitionAttributeDTO> attributes) {
        this.attributes = attributes;
    }
}
