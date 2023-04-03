package app.dto.service;

import app.model.service.Service;
import app.model.service.ServiceType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ServiceDTO {

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("service_name")
    private String serviceName;

    private String description;

    private boolean metadata;

    private ServiceType type;

//    String keywords;

    private String group;

    public ServiceDTO(Service service) {
        this.serviceCode = service.getServiceCode();
        this.serviceName = service.getServiceName();
        this.description = service.getDescription();
        this.metadata = service.isMetadata();
        this.type = service.getType();
        this.group = service.getServiceGroup();
//        this.keywords = service.getKeywords().toString(); // todo - fix lazy loading issue
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

//    public List<String> getKeywords() {
//        return keywords;
//    }
//
//    public void setKeywords(List<String> keywords) {
//        this.keywords = keywords;
//    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
