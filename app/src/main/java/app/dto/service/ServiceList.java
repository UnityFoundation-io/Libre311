package app.dto.service;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "services")
public class ServiceList {

    @JsonProperty("service")
    private List<ServiceDTO> services;

    public ServiceList(List<ServiceDTO> services) {
        this.services = services;
    }

    public List<ServiceDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceDTO> services) {
        this.services = services;
    }
}
