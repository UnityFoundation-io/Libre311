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

package app;

import app.dto.jurisdiction.CreateJurisdictionDTO;
import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.jurisdiction.PatchJurisdictionDTO;
import app.security.RequiresPermissions;
import app.service.jurisdiction.JurisdictionService;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.annotation.Nullable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

import static app.security.Permission.*;

@Controller("/api/tenant-admin")
public class TenantAdminController {

    private final JurisdictionService jurisdictionService;

    public TenantAdminController(JurisdictionService jurisdictionService) {
        this.jurisdictionService = jurisdictionService;
    }

    @Post(uris = {"/jurisdictions{?tenant_id}", "/jurisdictions.json{?tenant_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT})
    public JurisdictionDTO createJurisdictionJson(@Valid @Body CreateJurisdictionDTO requestDTO,
                                                   @Nullable @QueryValue("tenant_id") Long tenant_id) {
        return jurisdictionService.createJurisdiction(requestDTO, tenant_id);
    }

    @Post(uris = {"/jurisdictions/{jurisdictionId}/remote_hosts{?tenant_id}", "/jurisdictions.json/{jurisdictionId}/remote_hosts{?tenant_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT})
    public JurisdictionDTO createJurisdictionRemoteHostsJson(String jurisdictionId,
                                                             @Body Set<@NotBlank String> remoteHosts,
                                                             @Nullable @QueryValue("tenant_id") Long tenant_id) {
        return jurisdictionService.setJurisdictionRemoteHosts(jurisdictionId, remoteHosts);
    }

    @Patch(uris = {"/jurisdictions/{jurisdictionId}{?tenant_id}", "/jurisdictions/{jurisdictionId}.json{?tenant_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT})
    public JurisdictionDTO updateJurisdictionJson(String jurisdictionId, @Valid @Body PatchJurisdictionDTO requestDTO,
                                             @Nullable @QueryValue("tenant_id") Long tenant_id) {
        return jurisdictionService.updateJurisdiction(jurisdictionId, requestDTO);
    }
}
