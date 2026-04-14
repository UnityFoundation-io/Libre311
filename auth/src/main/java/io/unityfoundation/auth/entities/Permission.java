package io.unityfoundation.auth.entities;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity()
public class Permission {
  @Id
  @GeneratedValue
  private Long id;

  private String name;
  private String description;
  private PermissionScope scope;

  public enum PermissionScope {
    SYSTEM, TENANT, SUBTENANT
  }

}
