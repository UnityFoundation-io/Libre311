package app.servicerequest;

import app.servicedefinition.ServiceDTO;
import app.servicedefinition.ServiceList;
import app.recaptcha.CheckRecaptcha;
import app.servicedefinition.ServiceDefinitionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.xml.XmlEscapers;
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
import java.util.stream.Collectors;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
public class XmlController {

    private final ServiceDefinitionService serviceDefinitionService;
    private final ServiceRequestService serviceRequestService;

    public XmlController(ServiceDefinitionService serviceDefinitionService, ServiceRequestService serviceRequestService) {
        this.serviceDefinitionService = serviceDefinitionService;
        this.serviceRequestService = serviceRequestService;
    }

    @Get("/services.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> indexXml(@Valid Pageable pageable,
                                         @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        List<ServiceDTO> serviceDTOList = serviceDefinitionService.findAll(jurisdiction_id).stream()
                .map(serviceDTO -> {
                    if (serviceDTO.getDescription() != null) {
                        serviceDTO.setDescription(XmlEscapers.xmlContentEscaper().escape(serviceDTO.getDescription()));
                    }
                    return serviceDTO;
                }).collect(Collectors.toList());
        ServiceList serviceList = new ServiceList(serviceDTOList);

        return HttpResponse.ok(xmlMapper.writeValueAsString(serviceList));
    }

    @Get("/services/{serviceCode}.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceDefinitionXml(Long serviceCode,
                                          @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().build();
        return xmlMapper.writeValueAsString(serviceDefinitionService.getServiceDefinition(serviceCode, jurisdiction_id));
    }

    @Post("/requests.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    @CheckRecaptcha
    public String createServiceRequestXml(HttpRequest<?> request,
                                          @Valid @Body ServiceRequestPostRequest requestDTO,
                                          @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestService.createServiceRequest(request, requestDTO, jurisdiction_id)));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }

    @Get("/requests.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> getServiceRequestsXml(
            @Valid @RequestBean ServiceRequestListRequest requestDTO,
            @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id,
            HttpRequest<?> request)
            throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO,
                        jurisdiction_id, request.getHeaders().getAuthorization().orElse(null))
                .map(serviceRequestDTO -> {
                    sanitizeXmlContent(serviceRequestDTO);
                    return serviceRequestDTO;
                });;
        ServiceRequestList serviceRequestList = new ServiceRequestList(serviceRequestDTOPage.getContent());

        return HttpResponse.ok(xmlMapper.writeValueAsString(serviceRequestList))
                .headers(Map.of(
                        "Access-Control-Expose-Headers", "page-TotalSize, page-TotalPages, page-PageNumber, page-Offset, page-Size ",
                        "page-TotalSize", String.valueOf(serviceRequestDTOPage.getTotalSize()),
                        "page-TotalPages", String.valueOf(serviceRequestDTOPage.getTotalPages()),
                        "page-PageNumber", String.valueOf(serviceRequestDTOPage.getPageNumber()),
                        "page-Offset", String.valueOf(serviceRequestDTOPage.getOffset()),
                        "page-Size", String.valueOf(serviceRequestDTOPage.getSize())
                ));
    }
    @Get("/requests/{serviceRequestId}.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceRequestXml(Long serviceRequestId,
                                       @Nullable @QueryValue("jurisdiction_id") String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        ServiceRequestDTO serviceRequestDTO = serviceRequestService.getServiceRequest(serviceRequestId, jurisdiction_id);
        sanitizeXmlContent(serviceRequestDTO);
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestDTO));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }

    private void sanitizeXmlContent(ServiceRequestDTO serviceRequestDTO) {
        if (serviceRequestDTO.getDescription() != null) {
            serviceRequestDTO.setDescription(XmlEscapers.xmlContentEscaper().escape(serviceRequestDTO.getDescription()));
        }
        if (serviceRequestDTO.getAddress() != null) {
            serviceRequestDTO.setAddress(XmlEscapers.xmlContentEscaper().escape(serviceRequestDTO.getAddress()));
        }
    }
}
