package app.model.jurisdiction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class JurisdictionInfoResponse {
  private String id;
  private String name;

  @JsonProperty("auth_base_url")
  private String unityAuthUrl;

  public JurisdictionInfoResponse() {
  }

  public JurisdictionInfoResponse(String id, String name, String unityAuthUrl) {
    this.id = id;
    this.name = name;
    this.unityAuthUrl = unityAuthUrl;
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

  public String getUnityAuthUrl() {
    return unityAuthUrl;
  }

  public void setUnityAuthUrl(String unityAuthUrl) {
    this.unityAuthUrl = unityAuthUrl;
  }
}
