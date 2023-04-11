package app;

import app.dto.service.ServiceDTO;
import app.dto.service.ServiceList;
import app.dto.servicerequest.*;
import app.model.service.servicedefinition.ServiceDefinition;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller("/api")
public class RootController {

    private final ServiceService serviceService;
    private final ServiceRequestService serviceRequestService;

    public RootController(ServiceService serviceService, ServiceRequestService serviceRequestService) {
        this.serviceService = serviceService;
        this.serviceRequestService = serviceRequestService;
    }

    @Get(uris = {"/services", "/services.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceDTO>> indexJson(@Valid Pageable pageable) {
        Page<ServiceDTO> serviceDTOPage = serviceService.findAll(pageable);

        return HttpResponse.ok(serviceDTOPage.getContent())
                .headers(Map.of(
                        "Access-Control-Expose-Headers", "page-TotalSize, page-TotalPages, page-PageNumber, page-Offset, page-Size ",
                        "page-TotalSize", String.valueOf(serviceDTOPage.getTotalSize()),
                        "page-TotalPages", String.valueOf(serviceDTOPage.getTotalPages()),
                        "page-PageNumber", String.valueOf(serviceDTOPage.getPageNumber()),
                        "page-Offset", String.valueOf(serviceDTOPage.getOffset()),
                        "page-Size", String.valueOf(serviceDTOPage.getSize())
                ));
    }

    @Get("/services.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> indexXml(@Valid Pageable pageable) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        Page<ServiceDTO> serviceDTOPage = serviceService.findAll(pageable);
        ServiceList serviceList = new ServiceList(serviceDTOPage.getContent());

        return HttpResponse.ok(xmlMapper.writeValueAsString(serviceList))
                .headers(Map.of(
                        "Access-Control-Expose-Headers", "page-TotalSize, page-TotalPages, page-PageNumber, page-Offset, page-Size ",
                        "page-TotalSize", String.valueOf(serviceDTOPage.getTotalSize()),
                        "page-TotalPages", String.valueOf(serviceDTOPage.getTotalPages()),
                        "page-PageNumber", String.valueOf(serviceDTOPage.getPageNumber()),
                        "page-Offset", String.valueOf(serviceDTOPage.getOffset()),
                        "page-Size", String.valueOf(serviceDTOPage.getSize())
                ));
    }

    @Get(uris = {"/services/{serviceCode}", "/services/{serviceCode}.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceDefinitionJson(String serviceCode) {
        return serviceService.getServiceDefinition(serviceCode);
    }

    @Get("/services/{serviceCode}.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceDefinitionXml(String serviceCode) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().build();
        ObjectMapper objectMapper = new ObjectMapper();
        String serviceDefinitionStr = serviceService.getServiceDefinition(serviceCode);
        ServiceDefinition serviceDefinition = objectMapper.readValue(serviceDefinitionStr, ServiceDefinition.class);

        return xmlMapper.writeValueAsString(serviceDefinition);
    }

    @Post(uris = {"/requests", "/requests.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public List<PostResponseServiceRequestDTO> createServiceRequestJson(@Valid @Body PostRequestServiceRequestDTO requestDTO) {
        return List.of(serviceRequestService.createServiceRequest(requestDTO));
    }

    @Post("/requests.xml")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public String createServiceRequestXml(@Valid @Body PostRequestServiceRequestDTO requestDTO) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestService.createServiceRequest(requestDTO)));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }

    @Get(uris = {"/requests", "/requests.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceRequestDTO>> getServiceRequestsJson(@Valid @RequestBean GetServiceRequestsDTO requestDTO) {
        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO);
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

    @Get("/requests.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> getServiceRequestsXml(@Valid @RequestBean GetServiceRequestsDTO requestDTO) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO);
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

    @Get(uris = {"/requests/{serviceRequestId}", "/requests/{serviceRequestId}.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceRequestDTO> getServiceRequestJson(String serviceRequestId) {
        return List.of(serviceRequestService.getServiceRequest(serviceRequestId));
    }

    @Get("/requests/{serviceRequestId}.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceRequestXml(String serviceRequestId) throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestService.getServiceRequest(serviceRequestId)));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }
}
