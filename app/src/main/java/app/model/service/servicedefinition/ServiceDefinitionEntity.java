package app.model.service.servicedefinition;


import app.model.service.Service;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "service_definitions")
public class ServiceDefinitionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String version;

  private Boolean active;

  @Column(columnDefinition = "TEXT")
  @Convert(converter = ServiceDefinitionConverter.class)
  private ServiceDefinition definition;

  @ManyToOne
  private Service service;

  public ServiceDefinitionEntity() {
  }

  public ServiceDefinitionEntity(String version, Boolean active, ServiceDefinition definition) {
    this.version = version;
    this.active = active;
    this.definition = definition;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public ServiceDefinition getDefinition() {
    return definition;
  }

  public void setDefinition(ServiceDefinition definition) {
    this.definition = definition;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }
}
