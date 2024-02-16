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
import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.security.RequiresPermissions;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;

import static app.security.Permission.*;

@Controller("/api/admin")
@Tag(name = "Jurisdiction")
public class AdminConsoleController {

    private final ServiceService serviceService;
    private final ServiceRequestService serviceRequestService;

    public AdminConsoleController(ServiceService serviceService, ServiceRequestService serviceRequestService) {
        this.serviceService = serviceService;
        this.serviceRequestService = serviceRequestService;
    }

    @Post(uris = { "/services{?jurisdiction_id}", "/services.json{?jurisdiction_id}" })
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
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

    @Get(uris = { "/requests/{serviceRequestId}{?jurisdiction_id}",
            "/requests/{serviceRequestId}.json{?jurisdiction_id}" })
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_VIEW_SYSTEM, LIBRE311_REQUEST_VIEW_TENANT, LIBRE311_REQUEST_VIEW_SUBTENANT})
    public List<SensitiveServiceRequestDTO> getServiceRequestJson(Long serviceRequestId, @Nullable String jurisdiction_id) {
        return List.of(serviceRequestService.getSensitiveServiceRequest(serviceRequestId, jurisdiction_id));
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
