package app.dto.servicerequest;


import app.dto.service.ServiceDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "service_requests")
public class ServiceRequestList {

    @JsonProperty("request")
    private List<ServiceRequestDTO> serviceRequests;

    public ServiceRequestList(List<ServiceRequestDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public List<ServiceRequestDTO> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<ServiceRequestDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }
}
