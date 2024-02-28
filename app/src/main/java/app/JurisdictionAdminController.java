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

import app.dto.download.DownloadRequestsArgumentsDTO;
import app.dto.group.GroupDTO;
import app.dto.group.CreateUpdateGroupDTO;
import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.security.RequiresPermissions;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import static app.security.Permission.*;

@Controller("/api/jurisdiction-admin")
@Tag(name = "Jurisdiction")
public class JurisdictionAdminController {

    private final ServiceService serviceService;
    private final ServiceRequestService serviceRequestService;

    public JurisdictionAdminController(ServiceService serviceService, ServiceRequestService serviceRequestService) {
        this.serviceService = serviceService;
        this.serviceRequestService = serviceRequestService;
    }

    @Post(uris = { "/services{?jurisdiction_id}", "/services.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<ServiceDTO> createServiceJson(@Valid @Body CreateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return List.of(serviceService.createService(requestDTO, jurisdiction_id));
    }

    @Patch(uris = { "/services/{serviceId}{?jurisdiction_id}", "/requests/{serviceId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<ServiceDTO> updateServiceJson(Long serviceId, @Valid @Body UpdateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return List.of(serviceService.updateService(serviceId, requestDTO, jurisdiction_id));
    }

    @Get(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_VIEW_SYSTEM, LIBRE311_ADMIN_VIEW_TENANT, LIBRE311_ADMIN_VIEW_SUBTENANT})
    public Page<GroupDTO> indexGroups(@Valid Pageable pageable, @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.getPageableGroups(pageable, jurisdiction_id);
    }

    @Post(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<GroupDTO> createGroup(@Valid @Body CreateUpdateGroupDTO requestDTO,
                                      @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return List.of(serviceService.createGroup(requestDTO, jurisdiction_id));
    }

    @Patch(uris = { "/groups/{groupId}{?jurisdiction_id}", "/groups/{groupId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<GroupDTO> updateGroup(Long groupId, @Valid @Body CreateUpdateGroupDTO requestDTO,
                                              @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return List.of(serviceService.updateGroup(groupId, requestDTO));
    }

    @Delete(uris = { "/groups/{groupId}{?jurisdiction_id}", "/groups/{groupId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse deleteGroup(Long groupId,
                                  @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceService.deleteGroup(groupId);
        return HttpResponse.ok();
    }

    @Patch(uris = { "/requests/{serviceRequestId}{?jurisdiction_id}",
            "/requests/{serviceRequestId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_EDIT_SYSTEM, LIBRE311_REQUEST_EDIT_TENANT, LIBRE311_REQUEST_EDIT_SUBTENANT})
    public List<SensitiveServiceRequestDTO> updateServiceRequestJson(Long serviceRequestId, @Valid @Body PatchServiceRequestDTO requestDTO,
                                                                     @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return List.of(serviceRequestService.updateServiceRequest(serviceRequestId, requestDTO, jurisdiction_id));
    }

    @Get(value = "/requests/download{?jurisdiction_id}")
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_VIEW_SYSTEM, LIBRE311_REQUEST_VIEW_TENANT, LIBRE311_REQUEST_VIEW_SUBTENANT})
    public StreamedFile downloadServiceRequests(@Valid @RequestBean DownloadRequestsArgumentsDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws MalformedURLException {
        return serviceRequestService.getAllServiceRequests(requestDTO, jurisdiction_id);
    }
}
