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

import app.discovery.Discovery;
import app.jurisdiction.JurisdictionDTO;
import app.discovery.DiscoveryEndpointService;
import app.jurisdiction.JurisdictionService;
import app.servicedefinition.ServiceDefinitionService;
import app.servicerequest.ServiceRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;

import java.net.URI;
import java.net.URISyntaxException;

import static app.security.Permission.*;

@Controller("/api")
@Secured(SecurityRule.IS_ANONYMOUS)
public class RootController {

    private final static URI SWAGGER_UI = UriBuilder.of("/swagger-ui").path("index.html").build();

    private final ServiceDefinitionService serviceDefinitionService;
    private final ServiceRequestService serviceRequestService;
    private final DiscoveryEndpointService discoveryEndpointService;
    private final JurisdictionService jurisdictionService;

    public RootController(ServiceDefinitionService serviceDefinitionService, ServiceRequestService serviceRequestService,
                          DiscoveryEndpointService discoveryEndpointService, JurisdictionService jurisdictionService) {
        this.serviceDefinitionService = serviceDefinitionService;
        this.serviceRequestService = serviceRequestService;
        this.jurisdictionService = jurisdictionService;
        this.discoveryEndpointService = discoveryEndpointService;
    }

    @Get(uris = {"/discovery", "/discovery.json"})
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<Discovery> discoveryJson() {
        return HttpResponse.ok(discoveryEndpointService.getDiscoveryInfo());
    }

    @Get("/discovery.xml")
    @Produces(MediaType.TEXT_XML)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<String> discoveryXml() throws JsonProcessingException {
        XmlMapper xmlMapper = XmlMapper.xmlBuilder()
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .defaultUseWrapper(true).build();
        Discovery discovery = discoveryEndpointService.getDiscoveryInfo();

        return HttpResponse.ok(xmlMapper.writeValueAsString(discovery));
    }





    @Get(value =  "/config")
    @ExecuteOn(TaskExecutors.IO)
    public JurisdictionDTO getJurisdictionInfo(@Header("Referer") String referer) throws URISyntaxException {
        return jurisdictionService.findJurisdictionByHostName(new URI(referer).getHost());
    }

    @Get("/swagger-ui")
    @Hidden
    HttpResponse<?> home() {
        return HttpResponse.seeOther(SWAGGER_UI);
    }


}
