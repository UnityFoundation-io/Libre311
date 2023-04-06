package app.dto.servicerequest;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "service_requests")
public class ServiceRequestList {

    @JsonProperty("request")
    private List<? extends ServiceRequestResponseDTO> serviceRequests;

    public ServiceRequestList(List<? extends ServiceRequestResponseDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public List<? extends ServiceRequestResponseDTO> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<? extends ServiceRequestResponseDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }
}
