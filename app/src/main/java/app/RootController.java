// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app;

import app.dto.discovery.DiscoveryDTO;
import app.dto.download.DownloadRequestsArgumentsDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.ServiceList;
import app.dto.servicerequest.*;
import app.model.service.servicedefinition.ServiceDefinition;
import app.service.discovery.DiscoveryEndpointService;
import app.service.service.ServiceService;
import app.service.servicerequest.ServiceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.xml.XmlEscapers;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.annotation.Nullable;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
public class RootController {

    private final ServiceService serviceService;
    private final ServiceRequestService serviceRequestService;
    private final DiscoveryEndpointService discoveryEndpointService;

    public RootController(ServiceService serviceService, ServiceRequestService serviceRequestService,
                          DiscoveryEndpointService discoveryEndpointService) {
        this.serviceService = serviceService;
        this.serviceRequestService = serviceRequestService;
        this.discoveryEndpointService = discoveryEndpointService;
    }

    @Get(uris = {"/discovery", "/discovery.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<DiscoveryDTO> discoveryJson() {
        return HttpResponse.ok(discoveryEndpointService.getDiscoveryInfo());
    }

    @Get("/discovery.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> discoveryXml() throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .defaultUseWrapper(true).build();
        DiscoveryDTO discovery = discoveryEndpointService.getDiscoveryInfo();

        return HttpResponse.ok(xmlMapper.writeValueAsString(discovery));
    }

    @Get(uris = {"/services{?jurisdiction_id}", "/services.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceDTO>> indexJson(@Valid Pageable pageable, @Nullable String jurisdiction_id) {

        Page<ServiceDTO> serviceDTOPage = serviceService.findAll(pageable, jurisdiction_id);

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

    @Get("/services.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> indexXml(@Valid Pageable pageable, @Nullable String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        Page<ServiceDTO> serviceDTOPage = serviceService.findAll(pageable, jurisdiction_id)
                .map(serviceDTO -> {
                    if (serviceDTO.getDescription() != null) {
                        serviceDTO.setDescription(XmlEscapers.xmlContentEscaper().escape(serviceDTO.getDescription()));
                    }
                    return serviceDTO;
                });
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

    @Get(uris = {"/services/{serviceCode}{?jurisdiction_id}", "/services/{serviceCode}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceDefinitionJson(String serviceCode, @Nullable String jurisdiction_id) {

        return serviceService.getServiceDefinition(serviceCode, jurisdiction_id);
    }

    @Get("/services/{serviceCode}.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceDefinitionXml(String serviceCode, @Nullable String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().build();
        ObjectMapper objectMapper = new ObjectMapper();
        String serviceDefinitionStr = serviceService.getServiceDefinition(serviceCode, jurisdiction_id);
        ServiceDefinition serviceDefinition = objectMapper.readValue(serviceDefinitionStr, ServiceDefinition.class);

        return xmlMapper.writeValueAsString(serviceDefinition);
    }

    @Post(uris = {"/requests{?jurisdiction_id}", "/requests.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public List<PostResponseServiceRequestDTO> createServiceRequestJson(HttpRequest<?> request,
                                                                        @Valid @Body PostRequestServiceRequestDTO requestDTO,
                                                                        @Nullable String jurisdiction_id) {

        return List.of(serviceRequestService.createServiceRequest(request, requestDTO, jurisdiction_id));
    }

    @Post("/requests.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ExecuteOn(TaskExecutors.IO)
    public String createServiceRequestXml(HttpRequest<?> request,
                                          @Valid @Body PostRequestServiceRequestDTO requestDTO,
                                          @Nullable String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestService.createServiceRequest(request, requestDTO, jurisdiction_id)));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }

    @Get(uris = {"/requests{?jurisdiction_id}", "/requests.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<List<ServiceRequestDTO>> getServiceRequestsJson(@Valid @RequestBean GetServiceRequestsDTO requestDTO,
                                                                        @Nullable String jurisdiction_id) {

        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO, jurisdiction_id);
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

    @Get("/requests.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> getServiceRequestsXml(@Valid @RequestBean GetServiceRequestsDTO requestDTO,
                                                      @Nullable String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        Page<ServiceRequestDTO> serviceRequestDTOPage = serviceRequestService.findAll(requestDTO, jurisdiction_id)
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

    @Get(uris = {"/requests/{serviceRequestId}{?jurisdiction_id}", "/requests/{serviceRequestId}.json{?jurisdiction_id}"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public List<ServiceRequestDTO> getServiceRequestJson(Long serviceRequestId, @Nullable String jurisdiction_id) {

        return List.of(serviceRequestService.getServiceRequest(serviceRequestId, jurisdiction_id));
    }

    @Get("/requests/{serviceRequestId}.xml{?jurisdiction_id}")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public String getServiceRequestXml(Long serviceRequestId, @Nullable String jurisdiction_id) throws JsonProcessingException {

        XmlMapper xmlMapper = XmlMapper.xmlBuilder().defaultUseWrapper(false).build();
        xmlMapper.registerModule(new JavaTimeModule());
        ServiceRequestDTO serviceRequestDTO = serviceRequestService.getServiceRequest(serviceRequestId, jurisdiction_id);
        sanitizeXmlContent(serviceRequestDTO);
        ServiceRequestList serviceRequestList = new ServiceRequestList(List.of(serviceRequestDTO));

        return xmlMapper.writeValueAsString(serviceRequestList);
    }

    @Get(value =  "/requests/download{?jurisdiction_id}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile downloadServiceRequests(@Valid @RequestBean DownloadRequestsArgumentsDTO requestDTO, @Nullable String jurisdiction_id) throws MalformedURLException {
        return serviceRequestService.getAllServiceRequests(requestDTO, jurisdiction_id);
    }

    private void sanitizeXmlContent(ServiceRequestDTO serviceRequestDTO) {
        if (serviceRequestDTO.getDescription() != null) {
            serviceRequestDTO.setDescription(XmlEscapers.xmlContentEscaper().escape(serviceRequestDTO.getDescription()));
        }
        if (serviceRequestDTO.getStatusNotes() != null) {
            serviceRequestDTO.setStatusNotes(XmlEscapers.xmlContentEscaper().escape(serviceRequestDTO.getStatusNotes()));
        }
        if (serviceRequestDTO.getAddress() != null) {
            serviceRequestDTO.setAddress(XmlEscapers.xmlContentEscaper().escape(serviceRequestDTO.getAddress()));
        }
    }
}
