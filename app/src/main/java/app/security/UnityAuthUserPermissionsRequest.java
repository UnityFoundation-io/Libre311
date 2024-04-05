package app.security;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class UnityAuthUserPermissionsRequest {
    private Long tenantId;
    private Long serviceId;

    public UnityAuthUserPermissionsRequest(Long tenantId, Long serviceId) {
        this.tenantId = tenantId;
        this.serviceId = serviceId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
}
