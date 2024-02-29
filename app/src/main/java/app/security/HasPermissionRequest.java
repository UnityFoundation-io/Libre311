// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.security;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Introspected
public class HasPermissionRequest {

    @NotNull
    private Long tenantId;
    @NotNull
    private Long serviceId;
    private List<String> permissions;

    public HasPermissionRequest() {
    }

    public HasPermissionRequest(Long tenantId, Long serviceId, List<Permission> permissions) {
        this.tenantId = tenantId;
        this.serviceId = serviceId;
        this.permissions = permissions.stream()
                .map(Permission::getPermission).collect(Collectors.toList());
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

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}