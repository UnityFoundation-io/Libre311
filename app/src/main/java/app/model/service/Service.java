package app.model.service;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
public class Service {

    @Id
    private String serviceCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean metadata = false;

    @Enumerated(value = EnumType.STRING)
    private ServiceType type = ServiceType.REALTIME;

    // todo this will likely need to be a distinct list and stored in a many-to-many relationship
    @ElementCollection
    List<String> keywords = new ArrayList<String>();

    // todo this will need to be a many-to-one relationship. The list of groups should be distinct
    private String serviceGroup;


    public Service(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service() {}

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

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String group) {
        this.serviceGroup = group;
    }
}
