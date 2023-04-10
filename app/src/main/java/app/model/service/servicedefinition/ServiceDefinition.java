package app.model.service.servicedefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
@JacksonXmlRootElement(localName = "service_definitions")
public class ServiceDefinition {

    @JsonProperty("service_code")
    private String serviceCode;

    @JacksonXmlElementWrapper(localName = "attributes")
    @JacksonXmlProperty(localName = "attribute")
    private List<ServiceDefinitionAttribute> attributes;

    public ServiceDefinition() {
    }

    public List<ServiceDefinitionAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ServiceDefinitionAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
