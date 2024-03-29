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

import app.dto.group.GroupDTO;
import app.dto.group.CreateUpdateGroupDTO;
import app.dto.service.CreateServiceDTO;
import app.dto.service.PatchServiceOrderPositionDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicedefinition.CreateServiceDefinitionAttributeDTO;
import app.dto.servicedefinition.UpdateServiceDefinitionAttributeDTO;
import app.dto.servicerequest.GetServiceRequestsDTO;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.dto.servicedefinition.ServiceDefinitionDTO;
import app.security.RequiresPermissions;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
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
import java.util.UUID;

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
    public ServiceDTO createServiceJson(@Valid @Body CreateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        if (requestDTO.getServiceCode() == null) {
            requestDTO.setServiceCode(UUID.randomUUID().toString());
        }
        return serviceService.createService(requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/services/{serviceId}{?jurisdiction_id}", "/requests/{serviceId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDTO updateServiceJson(Long serviceId, @Valid @Body UpdateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.updateService(serviceId, requestDTO, jurisdiction_id);
    }

    @Post(uris = { "/services/{serviceId}/attributes{?jurisdiction_id}", "/services/{serviceId}/attributes.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDefinitionDTO addServiceDefinitionAttributeToServiceDefinition(Long serviceId,
                                                                                 @Valid @Body CreateServiceDefinitionAttributeDTO requestDTO,
                                                                                 @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.addServiceDefinitionAttributeToServiceDefinition(serviceId, requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/services/{serviceId}/attributes/{attributeId}{?jurisdiction_id}", "/services/{serviceId}/attributes/{attributeId}.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDefinitionDTO updateServiceDefinitionAttribute(Long serviceId, Long attributeId,
                                                                 @Valid @Body UpdateServiceDefinitionAttributeDTO requestDTO,
                                                                 @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.updateServiceDefinitionAttribute(attributeId, requestDTO);
    }

    @Delete(uris = { "/services/{serviceId}/attributes/{attributeId}{?jurisdiction_id}", "/services/{serviceId}/attributes/{attributeId}.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse<?> removeServiceDefinitionAttributeFromServiceDefinition(Long serviceId, Long attributeId,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceService.removeServiceDefinitionAttributeFromServiceDefinition(attributeId);
        return HttpResponse.ok();
    }

    @Get(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_VIEW_SYSTEM, LIBRE311_ADMIN_VIEW_TENANT, LIBRE311_ADMIN_VIEW_SUBTENANT})
    public List<GroupDTO> indexGroups(@Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.getListGroups(jurisdiction_id);
    }

    @Post(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public GroupDTO createGroup(@Valid @Body CreateUpdateGroupDTO requestDTO,
                                      @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.createGroup(requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/groups/{groupId}{?jurisdiction_id}", "/groups/{groupId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public GroupDTO updateGroup(Long groupId, @Valid @Body CreateUpdateGroupDTO requestDTO,
                                              @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.updateGroup(groupId, requestDTO);
    }

    @Patch(uris = { "/groups/{groupId}/services-order{?jurisdiction_id}", "/groups/{groupId}/services-order.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<ServiceDTO> updateServicesOrder(Long groupId, @Valid @Body List<PatchServiceOrderPositionDTO> requestDTO,
                                              @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceService.updateServiceOrderPositions(groupId, requestDTO);
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
    public SensitiveServiceRequestDTO updateServiceRequestJson(Long serviceRequestId, @Valid @Body PatchServiceRequestDTO requestDTO,
                                                                     @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceRequestService.updateServiceRequest(serviceRequestId, requestDTO, jurisdiction_id);
    }

    @Delete(uris = { "/services/{serviceId}{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse deleteService(Long serviceId, @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceService.deleteService(serviceId, jurisdiction_id);
        return HttpResponse.ok();
    }

    @Get(value = "/requests/download{?jurisdiction_id}")
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_VIEW_SYSTEM, LIBRE311_REQUEST_VIEW_TENANT, LIBRE311_REQUEST_VIEW_SUBTENANT})
    public StreamedFile downloadServiceRequests(@Valid @RequestBean GetServiceRequestsDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws MalformedURLException {
        return serviceRequestService.getAllServiceRequests(requestDTO, jurisdiction_id);
    }
}
