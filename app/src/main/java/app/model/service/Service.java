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
}
