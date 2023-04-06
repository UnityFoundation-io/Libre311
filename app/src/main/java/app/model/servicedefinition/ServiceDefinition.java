package app.model.servicedefinition;

import app.model.service.Service;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "service_definitions")
public class ServiceDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @MapsId
    @OneToOne
    private Service service;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "serviceDefinition")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ServiceDefinitionAttribute> attributes;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<ServiceDefinitionAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ServiceDefinitionAttribute> attributes) {
        this.attributes = attributes;
    }
}
