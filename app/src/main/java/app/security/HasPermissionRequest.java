package app.security;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotNull;
import java.util.List;

@Introspected
public class HasPermissionRequest {

    @NotNull
    private String tenantId;
    @NotNull
    private String serviceId;
    private List<String> permissions;

    public HasPermissionRequest() {
    }

    public HasPermissionRequest(String tenantId, String serviceId, List<String> permissions) {
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.permissions = permissions;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
