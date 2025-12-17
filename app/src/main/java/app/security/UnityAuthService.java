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

import app.jurisdiction.Jurisdiction;
import app.jurisdiction.JurisdictionRepository;
import app.security.user.User;
import app.security.user.UserRepository;
import app.security.user.JurisdictionUser;
import app.security.user.JurisdictionUserRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Singleton
public class UnityAuthService {
    private static final Logger LOG = LoggerFactory.getLogger(UnityAuthService.class);

    @Property(name = "app.service-id")
    protected Long serviceId;

    private final UnityAuthClient client;
    private final UserRepository userRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final JurisdictionUserRepository jurisdictionUserRepository;


    public UnityAuthService(UnityAuthClient client, UserRepository userRepository,
                            JurisdictionRepository jurisdictionRepository,
                            JurisdictionUserRepository jurisdictionUserRepository) {
        this.client = client;
        this.userRepository = userRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.jurisdictionUserRepository = jurisdictionUserRepository;
    }

    public HttpResponse<UserPermissionsResponse> getUserPermissions(
        @PathVariable String jurisdictionId, String authorization) {
        Jurisdiction jurisdiction = jurisdictionRepository.findByJurisdictionId(jurisdictionId);
        // later on we will augment the permissions returned by unityAuthClient with any Libre defined permissions.  Currently, there are none so we simply proxy the call to unity auth service.
        return this.client.getUserPermissions(
            new UnityAuthUserPermissionsRequest(jurisdiction.getTenantId(), serviceId),
            authorization);
    }

    public boolean isUserPermittedForTenantAction(String token, Long tenantId, List<Permission> permissions) {

        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(tenantId, serviceId, permissions);

        boolean hasPermission;
        try {
            HttpResponse<HasPermissionResponse> hasPermissionResponseHttpResponse = client.hasPermission(hasPermissionRequest, token);
            Optional<HasPermissionResponse> body = hasPermissionResponseHttpResponse.getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                return false;
            }

            hasPermission = body.get().isHasPermission() && validateTenantPermissions(body.get().getPermissions());
        } catch (HttpClientResponseException e) {
            Optional<HasPermissionResponse> body = e.getResponse().getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                LOG.error("Returned {}", e.getMessage());
            } else {
                LOG.error("Returned {}", (body.get().getErrorMessage() == null ? e.getMessage() : e.getStatus()));
            }

            return false;
        }

        return hasPermission;
    }

    private boolean validateTenantPermissions(List<String> permissions) {
        return permissions != null && permissions.stream().anyMatch(s -> s.endsWith("-SYSTEM") || s.endsWith("-TENANT"));
    }

    public boolean isUserPermittedForJurisdictionAction(String token, String jurisdictionId, List<Permission> permissions) {

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById(jurisdictionId);
        if (optionalJurisdiction.isEmpty()) {
            return false;
        }

        Long tenantId = optionalJurisdiction.get().getTenantId();
        HasPermissionRequest hasPermissionRequest = new HasPermissionRequest(tenantId, serviceId, permissions);

        boolean hasPermission;
        try {
            HttpResponse<HasPermissionResponse> hasPermissionResponseHttpResponse = client.hasPermission(hasPermissionRequest, token);
            Optional<HasPermissionResponse> body = hasPermissionResponseHttpResponse.getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                return false;
            }

            hasPermission = body.get().isHasPermission() &&
                    validateUserExistenceAndPermissions(body.get().getUserEmail(), body.get().getPermissions(), jurisdictionId);
        } catch (HttpClientResponseException e) {
            Optional<HasPermissionResponse> body = e.getResponse().getBody(HasPermissionResponse.class);
            if (body.isEmpty()) {
                LOG.error("Returned {}", e.getMessage());
            } else {
                LOG.error("Returned {}", (body.get().getErrorMessage() == null ? e.getMessage() : e.getStatus()));
            }

            return false;
        }

        return hasPermission;
    }

    private boolean validateUserExistenceAndPermissions(String userEmail, List<String> permissions, String jurisdictionId) {
        if (permissions != null && permissions.stream().anyMatch(s -> s.endsWith("-SYSTEM") || s.endsWith("-TENANT"))) {
            return true;
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        Optional<JurisdictionUser> jurisdictionUserOptional = jurisdictionUserRepository.findByUserAndJurisdictionId(user, jurisdictionId);
        if (jurisdictionUserOptional.isEmpty()) {
            return false;
        }

        if (permissions != null && permissions.stream().anyMatch(s -> s.contains("_ADMIN_"))) {
            return jurisdictionUserOptional.get().isUserAdmin();
        }

        return true;
    }
}