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

import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.service.jurisdiction.JurisdictionService;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller("/api/admin")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AdminConsoleController {

    private final ServiceService serviceService;
    private final ServiceRequestService serviceRequestService;
    private final JurisdictionService jurisdictionService;

    public AdminConsoleController(ServiceService serviceService, ServiceRequestService serviceRequestService, JurisdictionService jurisdictionService) {
        this.serviceService = serviceService;
        this.serviceRequestService = serviceRequestService;
        this.jurisdictionService = jurisdictionService;
    }

    @Post(uris = {"/services", "/services.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceDTO> createServiceJson(@Valid @Body CreateServiceDTO requestDTO) {
        List<Map> errors = jurisdictionService.validateJurisdictionSupport(requestDTO.getJurisdictionId());
        if (!errors.isEmpty()) {
            return null;
        }

        return List.of(serviceService.createService(requestDTO));
    }

    @Patch(uris = {"/services/{serviceId}", "/requests/{serviceId}.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceDTO> updateServiceJson(Long serviceId, @Valid @Body UpdateServiceDTO requestDTO) {
        List<Map> errors = jurisdictionService.validateJurisdictionSupport(requestDTO.getJurisdictionId());
        if (!errors.isEmpty()) {
            return null;
        }

        return List.of(serviceService.updateService(serviceId, requestDTO));
    }

    @Get(uris = {"/requests/{serviceRequestId}{?jurisdiction_id}", "/requests/{serviceRequestId}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public List<SensitiveServiceRequestDTO> getServiceRequestJson(Long serviceRequestId, @Nullable String jurisdiction_id) {
        List<Map> errors = jurisdictionService.validateJurisdictionSupport(jurisdiction_id);
        if (!errors.isEmpty()) {
            return null;
        }

        return List.of(serviceRequestService.getSensitiveServiceRequest(serviceRequestId, jurisdiction_id));
    }

    @Patch(uris = {"/requests/{serviceRequestId}", "/requests/{serviceRequestId}.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public List<SensitiveServiceRequestDTO> updateServiceRequestJson(Long serviceRequestId, @Valid @Body PatchServiceRequestDTO requestDTO) {
        List<Map> errors = jurisdictionService.validateJurisdictionSupport(requestDTO.getJurisdictionId());
        if (!errors.isEmpty()) {
            return null;
        }

        return List.of(serviceRequestService.updateServiceRequest(serviceRequestId, requestDTO));
    }
}
