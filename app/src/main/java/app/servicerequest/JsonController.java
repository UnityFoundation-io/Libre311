package app.servicerequest;

import app.servicedefinition.ServiceDTO;
import app.servicedefinition.ServiceDefinitionDTO;
import app.recaptcha.CheckRecaptcha;
import app.security.RequiresPermissions;
import app.servicedefinition.ServiceDefinitionService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import static app.security.Permission.*;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
public class JsonController {

    private final ServiceDefinitionService serviceDefinitionService;
    private final ServiceRequestService serviceRequestService;

    public JsonController(ServiceDefinitionService serviceDefinitionService, ServiceRequestService serviceRequestService) {
        this.serviceDefinitionService = serviceDefinitionService;
        this.serviceRequestService = serviceRequestService;
    }

    @Get(uris = {"/services{?jurisdiction_id}", "/services.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceDTO>> indexJson(@Valid Pageable pageable,
                                                    @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {

        return HttpResponse.ok(serviceDefinitionService.findAll(jurisdiction_id));
    }



    @Get(uris = {"/services/{serviceCode}{?jurisdiction_id}", "/services/{serviceCode}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public ServiceDefinitionDTO getServiceDefinitionJson(Long serviceCode,
                                                         @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {

        return serviceDefinitionService.getServiceDefinition(serviceCode, jurisdiction_id);
    }



    @Post(uris = {"/requests{?jurisdiction_id}", "/requests.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    @CheckRecaptcha
    public List<ServiceRequestPostResponse> createServiceRequestJson(HttpRequest<?> request,
                                                                     @Valid @Body ServiceRequestPostRequest requestDTO,
                                                                     @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {

        return List.of(serviceRequestService.createServiceRequest(request, requestDTO, jurisdiction_id));
    }



    @Get(uris = {"/requests{?jurisdiction_id}", "/requests.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceRequestDTO>> getServiceRequestsJson(
            @Valid @RequestBean ServiceRequestListRequest requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id,
            HttpRequest<?> request) {
        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO,
                jurisdiction_id, request.getHeaders().getAuthorization().orElse(null));
        return HttpResponse.ok(serviceRequestDTOPage.getContent())
                .headers(Map.of(
                        "Access-Control-Expose-Headers", "page-TotalSize, page-TotalPages, page-PageNumber, page-Offset, page-Size ",
                        "page-TotalSize", String.valueOf(serviceRequestDTOPage.getTotalSize()),
                        "page-TotalPages", String.valueOf(serviceRequestDTOPage.getTotalPages()),
                        "page-PageNumber", String.valueOf(serviceRequestDTOPage.getPageNumber()),
                        "page-Offset", String.valueOf(serviceRequestDTOPage.getOffset()),
                        "page-Size", String.valueOf(serviceRequestDTOPage.getSize())
                ));
    }



    @Get(uris = {"/requests/{serviceRequestId}{?jurisdiction_id}", "/requests/{serviceRequestId}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceRequestDTO> getServiceRequestJson(Long serviceRequestId,
                                                         @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) {

        return List.of(serviceRequestService.getServiceRequest(serviceRequestId, jurisdiction_id));
    }

    @Delete(uris = {"/requests/{service_request_id}{?jurisdiction_id}", "/requests/{service_request_id}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_REQUEST_EDIT_SYSTEM, LIBRE311_REQUEST_EDIT_TENANT, LIBRE311_REQUEST_EDIT_SUBTENANT})
    public Map<String, Boolean> deleteServiceRequest(@PathVariable("service_request_id") Long serviceRequestId, @Nullable @QueryValue("jurisdiction_id") String jurisdictionId){
        return Map.of("success", serviceRequestService.delete(serviceRequestId, jurisdictionId) > 0);
    }
}
