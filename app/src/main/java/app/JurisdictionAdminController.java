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

import app.servicedefinition.group.GroupDTO;
import app.servicedefinition.group.CreateUpdateGroupDTO;
import app.servicedefinition.CreateServiceDTO;
import app.servicedefinition.PatchServiceOrderPositionDTO;
import app.servicedefinition.ServiceDTO;
import app.servicedefinition.UpdateServiceDTO;
import app.servicedefinition.CreateServiceDefinitionAttributeDTO;
import app.servicedefinition.PatchAttributeOrderDTO;
import app.servicedefinition.UpdateServiceDefinitionAttributeDTO;
import app.servicerequest.ServiceRequestListRequest;
import app.servicerequest.ServiceRequestUpdateRequest;
import app.servicerequest.SensitiveServiceRequestDTO;
import app.servicedefinition.ServiceDefinitionDTO;
import app.security.RequiresPermissions;
import app.servicedefinition.ServiceDefinitionService;
import app.servicerequest.ServiceRequestService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;

import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;

import static app.security.Permission.*;

@Controller("/api/jurisdiction-admin")
@Tag(name = "Jurisdiction")
public class JurisdictionAdminController {

    private final ServiceDefinitionService serviceDefinitionService;
    private final ServiceRequestService serviceRequestService;

    public JurisdictionAdminController(ServiceDefinitionService serviceDefinitionService, ServiceRequestService serviceRequestService) {
        this.serviceDefinitionService = serviceDefinitionService;
        this.serviceRequestService = serviceRequestService;
    }

    @Post(uris = { "/services{?jurisdiction_id}", "/services.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDTO createServiceJson(@Valid @Body CreateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.createService(requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/services/{serviceCode}{?jurisdiction_id}", "/requests/{serviceCode}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDTO updateServiceJson(Long serviceCode, @Valid @Body UpdateServiceDTO requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.updateService(serviceCode, requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/groups/{groupId}/services-order{?jurisdiction_id}", "/groups/{groupId}/services-order.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public List<ServiceDTO> updateServicesOrder(Long groupId, @Valid @Body List<PatchServiceOrderPositionDTO> requestDTO,
                                                @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.updateServiceOrderPositions(groupId, requestDTO);
    }

    @Post(uris = { "/services/{serviceCode}/attributes{?jurisdiction_id}", "/services/{serviceCode}/attributes.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDefinitionDTO addServiceDefinitionAttributeToServiceDefinition(Long serviceCode,
                                                                                 @Valid @Body CreateServiceDefinitionAttributeDTO requestDTO,
                                                                                 @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.addServiceDefinitionAttributeToServiceDefinition(serviceCode, requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/services/{serviceCode}/attributes/{attributeCode}{?jurisdiction_id}", "/services/{serviceCode}/attributes/{attributeCode}.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDefinitionDTO updateServiceDefinitionAttribute(Long serviceCode, Long attributeCode,
                                                                 @Valid @Body UpdateServiceDefinitionAttributeDTO requestDTO,
                                                                 @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.updateServiceDefinitionAttribute(attributeCode, requestDTO);
    }

    @Delete(uris = { "/services/{serviceCode}/attributes/{attributeCode}{?jurisdiction_id}", "/services/{serviceCode}/attributes/{attributeCode}.json{?jurisdiction_id}"})
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse<?> removeServiceDefinitionAttributeFromServiceDefinition(Long serviceCode, Long attributeCode,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceDefinitionService.removeServiceDefinitionAttributeFromServiceDefinition(attributeCode);
        return HttpResponse.ok();
    }

    @Patch(uris = { "/services/{serviceCode}/attributes-order{?jurisdiction_id}", "/services/{serviceCode}/attributes-order.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ServiceDefinitionDTO updateAttributesOrder(Long serviceCode, @Valid @Body List<PatchAttributeOrderDTO> requestDTO,
                                                @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.updateAttributesOrder(serviceCode, requestDTO);
    }

    @Get(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_VIEW_SYSTEM, LIBRE311_ADMIN_VIEW_TENANT, LIBRE311_ADMIN_VIEW_SUBTENANT})
    public List<GroupDTO> indexGroups(@Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.getListGroups(jurisdiction_id);
    }

    @Post(uris = { "/groups{?jurisdiction_id}", "/groups.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public GroupDTO createGroup(@Valid @Body CreateUpdateGroupDTO requestDTO,
                                      @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.createGroup(requestDTO, jurisdiction_id);
    }

    @Patch(uris = { "/groups/{groupId}{?jurisdiction_id}", "/groups/{groupId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public GroupDTO updateGroup(Long groupId, @Valid @Body CreateUpdateGroupDTO requestDTO,
                                              @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceDefinitionService.updateGroup(groupId, requestDTO);
    }

    @Delete(uris = { "/groups/{groupId}{?jurisdiction_id}", "/groups/{groupId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse deleteGroup(Long groupId,
                                  @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceDefinitionService.deleteGroup(groupId);
        return HttpResponse.ok();
    }

    @Patch(uris = { "/requests/{serviceRequestId}{?jurisdiction_id}",
            "/requests/{serviceRequestId}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_EDIT_SYSTEM, LIBRE311_REQUEST_EDIT_TENANT, LIBRE311_REQUEST_EDIT_SUBTENANT})
    public SensitiveServiceRequestDTO updateServiceRequestJson(Long serviceRequestId, @Valid @Body ServiceRequestUpdateRequest updateRequest,
                                                                     @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        return serviceRequestService.updateServiceRequest(serviceRequestId, updateRequest, jurisdiction_id);
    }

    @Delete(uris = { "/services/{serviceCode}{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse deleteService(Long serviceCode, @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {
        serviceDefinitionService.deleteService(serviceCode, jurisdiction_id);
        return HttpResponse.ok();
    }

    @Get(value = "/requests/download{?jurisdiction_id}")
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_VIEW_SYSTEM, LIBRE311_REQUEST_VIEW_TENANT, LIBRE311_REQUEST_VIEW_SUBTENANT})
    public StreamedFile downloadServiceRequests(@Valid @RequestBean ServiceRequestListRequest requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws MalformedURLException {
        return serviceRequestService.getAllServiceRequests(requestDTO, jurisdiction_id);
    }
}
