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

import io.micronaut.core.annotation.Nullable;

import java.util.List;

@Introspected
public class HasPermissionResponse {
    private boolean hasPermission;

    @Nullable
    private String userEmail;

    @Nullable
    private String errorMessage;

    private List<String> permissions;

    public HasPermissionResponse() {
    }

    public HasPermissionResponse(boolean hasPermission, @Nullable String userEmail, @Nullable String errorMessage, List<String> permissions) {
        this.hasPermission = hasPermission;
        this.userEmail = userEmail;
        this.errorMessage = errorMessage;
        this.permissions = permissions;
    }

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    @Nullable
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(@Nullable String userEmail) {
        this.userEmail = userEmail;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(@Nullable String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}