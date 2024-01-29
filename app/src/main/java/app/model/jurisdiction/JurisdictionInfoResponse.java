package app.model.jurisdiction;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class JurisdictionInfoResponse {
  private String id;
  private String name;

  public JurisdictionInfoResponse() {
  }

  public JurisdictionInfoResponse(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
