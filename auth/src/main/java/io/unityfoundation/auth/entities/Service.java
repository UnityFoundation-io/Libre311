package io.unityfoundation.auth.entities;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity()
public class Service {
  @Id
  @GeneratedValue
  private Long id;

  private String name;
  private String description;
  private ServiceStatus status;

  public enum ServiceStatus {
    ENABLED, DISABLED, DOWN_FOR_MAINTENANCE
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ServiceStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceStatus status) {
    this.status = status;
  }

}
