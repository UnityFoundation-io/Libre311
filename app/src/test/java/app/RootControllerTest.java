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

import app.dto.discovery.DiscoveryDTO;
import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.service.ServiceDTO;
import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.dto.servicerequest.ServiceRequestDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdiction.LatLong;
import app.model.jurisdiction.RemoteHost;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.dto.servicedefinition.ServiceDefinitionAttributeDTO;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.security.HasPermissionResponse;
import app.security.Permission;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments={"app-api-test-data"}, transactional = false)
public class RootControllerTest {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    DbCleanup dbCleanup;

    @BeforeEach
    void setup() {
        dbCleanup.cleanupServiceRequests();
    }

    private void setAuthHasPermissionSuccessResponse(boolean success, List<Permission> permissions) {
        var permissionsAsString = permissions.stream()
            .map(Permission::getPermission).collect(Collectors.toList());
        if (success) {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(true, "person1@test.io", null, permissionsAsString)));
        } else {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(false, "person1@test.io", "Unauthorized", permissionsAsString)));
        }
    }

    void authLogin() {
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionSuccessResponse(true, List.of());
    }

    // create
    @Test
    public void canCreateServiceRequestThatDoesNotRequireAdditionalAttributes() {
        HttpResponse<?> response;

        response = createServiceRequest("006", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));
    }

    @Test
    public void canCreateServiceRequestWithRequiredAttributes() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));
    }

    @Test
    public void canCreateServiceRequestWithVaryingDatatypes() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of(
                        "attribute[SDWLK]", "NARROW",
                        "attribute[SDWLK_NEAR]", "A string description",
                        "attribute[SDWLK_WIDTH]", "5",
                        "attribute[SDWLK_DATETIME]", "2015-04-14T11:07:36.639Z",
                        "attribute[SDWLK_CMNTS]", "This is a comment field that can introduce multiline characters",
                        "attribute[SDWLK_SNGLIST]", "NARROW"
                ),
                "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        assertTrue(Arrays.stream(postResponseServiceRequestDTOS)
                .anyMatch(postResponseServiceRequestDTO -> postResponseServiceRequestDTO.getId() != null));

        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];


        // GET
        response = client.toBlocking().exchange("/requests/" + postResponseServiceRequestDTO.getId() + "?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceRequestDTO[]> bodyOptional = response.getBody(ServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);

        ServiceRequestDTO serviceRequestDTO = serviceRequestDTOS[0];
        assertFalse(serviceRequestDTO.getSelectedValues().isEmpty());
        ServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO = serviceRequestDTO.getSelectedValues().get(0);
        assertNotNull(serviceDefinitionAttributeDTO.getValues());
        assertFalse(serviceDefinitionAttributeDTO.getValues().isEmpty());
    }

    @Test
    public void cannotCreateServiceRequestWithInvalidFormattedDateField() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest("001", "12345 Fairway",
                    Map.of(
                            "attribute[SDWLK]", "NARROW",
                            "attribute[SDWLK_DATETIME]", "0015/04/14Z"
                    ),
                    "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithInvalidFormattedNumberField() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest("001", "12345 Fairway",
                    Map.of(
                            "attribute[SDWLK]", "NARROW",
                            "attribute[SDWLK_WIDTH]", "NotANumber"
                    ),
                    "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithoutRequiredAttributes() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest("001", "12345 Fairway", Map.of(), "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithBlankJurisdiction() {
        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO("006");
        serviceRequestDTO.setgRecaptchaResponse("abc");
        serviceRequestDTO.setAddressString("12345 Fairway");
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);

        HttpRequest<?> request = HttpRequest.POST("/requests?jurisdiction_id=", payload)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, Map.class);
        });
        assertEquals(BAD_REQUEST, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestIfLatLngNotProvided() {
        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO("006");
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest(serviceRequestDTO, Map.of(), "town.gov");
        });
        assertEquals(BAD_REQUEST, thrown.getStatus());
    }

    // list + read
    @Test
    public void canListServicesByJurisdictionId() {
        HttpResponse<?> response;

        // without jurisdiction should fail
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange("/services", ServiceDTO[].class);
        });
        assertEquals(BAD_REQUEST, thrown.getStatus());

        response = client.toBlocking().exchange("/services?jurisdiction_id=city.gov", ServiceDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceDTO[]> bodyOptional = response.getBody(ServiceDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceDTO[] serviceDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceDTOS).findAny().isPresent());
    }

    @Test
    public void canGetServiceDefinitionByServiceCodeAndRequiredJurisdictionId() {
        HttpResponse<?> response;

        response = client.toBlocking().exchange("/services/001?jurisdiction_id=city.gov", String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinition = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinition));
    }

    @Test
    public void canListServiceRequestsByJurisdiction() {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("006", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = client.toBlocking().exchange("/requests?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceRequestDTO[]> bodyOptional = response.getBody(ServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
        assertTrue(Arrays.stream(serviceRequestDTOS).allMatch(
                serviceRequestDTO -> "city.gov".equals(serviceRequestDTO.getJurisdictionId())));
    }

    @Test
    public void authenticatedUsersCanViewSensitiveServiceRequestDetails() {
        setAuthHasPermissionSuccessResponse(true, List.of(Permission.LIBRE311_REQUEST_VIEW_TENANT));

        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO("001");
        serviceRequestDTO.setgRecaptchaResponse("abc");
        serviceRequestDTO.setLongitude("43.3434");
        serviceRequestDTO.setLatitude("48.98");
        serviceRequestDTO.setEmail("private@test.com");

        createServiceRequest(serviceRequestDTO, Map.of("attribute[SDWLK]", "NARROW"),
            "city.gov");

        var req = HttpRequest.GET("/requests?jurisdiction_id=city.gov").bearerAuth( "eyekljdsl");
        HttpResponse<List<SensitiveServiceRequestDTO>> response = client.toBlocking().exchange(req,
            Argument.listOf(SensitiveServiceRequestDTO.class));
        assertEquals(HttpStatus.OK, response.status());
        assertEquals(serviceRequestDTO.getEmail(), response.getBody().orElseThrow().get(0).getEmail());
    }

    @Test
    public void canFilterServiceRequests() {
        setAuthHasPermissionSuccessResponse(true, List.of(Permission.LIBRE311_REQUEST_VIEW_TENANT));

        Optional<Service> optionalService = serviceRepository.findByServiceCodeAndJurisdictionId("001", "city.gov");

        ServiceRequest closedHighPriority = new ServiceRequest();
        closedHighPriority.setStatus(ServiceRequestStatus.CLOSED);
        closedHighPriority.setPriority(ServiceRequestPriority.HIGH);
        closedHighPriority.setService(optionalService.get());
        closedHighPriority.setJurisdiction(optionalService.get().getJurisdiction());
        closedHighPriority.setLatitude("12.34");
        closedHighPriority.setLongitude("56.78");
        ServiceRequest closedHighSR = serviceRequestRepository.save(closedHighPriority);

        ServiceRequest openLowPriority = new ServiceRequest();
        openLowPriority.setStatus(ServiceRequestStatus.OPEN);
        openLowPriority.setPriority(ServiceRequestPriority.LOW);
        openLowPriority.setService(optionalService.get());
        openLowPriority.setJurisdiction(optionalService.get().getJurisdiction());
        openLowPriority.setLatitude("12.34");
        openLowPriority.setLongitude("56.78");
        ServiceRequest openLowSR = serviceRequestRepository.save(openLowPriority);

        ServiceRequest assignedMedium = new ServiceRequest();
        assignedMedium.setStatus(ServiceRequestStatus.ASSIGNED);
        assignedMedium.setPriority(ServiceRequestPriority.MEDIUM);
        assignedMedium.setService(optionalService.get());
        assignedMedium.setJurisdiction(optionalService.get().getJurisdiction());
        assignedMedium.setLatitude("12.34");
        assignedMedium.setLongitude("56.78");
        serviceRequestRepository.save(assignedMedium);

        // filter high priority
        HttpRequest<?> req = HttpRequest.GET("/requests?jurisdiction_id=city.gov&priority=high").bearerAuth( "eyekljdsl");
        HttpResponse<List<SensitiveServiceRequestDTO>> response = client.toBlocking().exchange(req,
            Argument.listOf(SensitiveServiceRequestDTO.class));
        assertEquals(HttpStatus.OK, response.status());
        Optional<List<SensitiveServiceRequestDTO>> body = response.getBody(Argument.listOf(SensitiveServiceRequestDTO.class));
        assertTrue(body.isPresent());
        assertFalse(body.get().isEmpty());
        assertTrue(body.get().stream().anyMatch(sensitiveServiceRequestDTO -> sensitiveServiceRequestDTO.getId().equals(closedHighSR.getId())));
        assertEquals(1, body.get().size());

        // filter open status
        req = HttpRequest.GET("/requests?jurisdiction_id=city.gov&status=open").bearerAuth( "eyekljdsl");
        response = client.toBlocking().exchange(req,
                Argument.listOf(SensitiveServiceRequestDTO.class));
        assertEquals(HttpStatus.OK, response.status());
        body = response.getBody(Argument.listOf(SensitiveServiceRequestDTO.class));
        assertTrue(body.isPresent());
        assertFalse(body.get().isEmpty());
        assertTrue(body.get().stream().anyMatch(sensitiveServiceRequestDTO -> sensitiveServiceRequestDTO.getId().equals(openLowSR.getId())));
        assertEquals(1, body.get().size());

        // filter high and low priority
        req = HttpRequest.GET("/requests?jurisdiction_id=city.gov&priority=low&priority=high").bearerAuth( "eyekljdsl");
        response = client.toBlocking().exchange(req,
                Argument.listOf(SensitiveServiceRequestDTO.class));
        assertEquals(HttpStatus.OK, response.status());
        body = response.getBody(Argument.listOf(SensitiveServiceRequestDTO.class));
        assertTrue(body.isPresent());
        assertFalse(body.get().isEmpty());
        assertTrue(body.get().stream().anyMatch(sensitiveServiceRequestDTO ->
                sensitiveServiceRequestDTO.getId().equals(openLowSR.getId()) ||
                        sensitiveServiceRequestDTO.getId().equals(closedHighSR.getId())));
        assertEquals(2, body.get().size());
    }

    @Test
    public void canGetAServiceRequestWithJurisdictionId() {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];

        response = client.toBlocking().exchange("/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<ServiceRequestDTO[]> bodyOptional = response.getBody(ServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        ServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
    }

    @Test
    public void canGetDiscoveryInfoJson() {
        HttpRequest<?> request = HttpRequest.GET("/discovery");
        HttpResponse<?> response = client.toBlocking().exchange(request, Map.class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<DiscoveryDTO> bodyOptional = response.getBody(DiscoveryDTO.class);
        assertTrue(bodyOptional.isPresent());
        assertNotNull(bodyOptional.get().getContact());
    }

    @Test
    public void canGetDiscoveryInfoXml() {
        HttpRequest<?> request = HttpRequest.GET("/discovery.xml");
        HttpResponse<?> response = client.toBlocking().exchange(request, String.class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<String> bodyOptional = response.getBody(String.class);
        assertTrue(bodyOptional.isPresent());
        assertNotNull(bodyOptional.get());
    }

    @Test
    public void getJurisdictionTest() {
        Jurisdiction j = new Jurisdiction("1", 1L, "jurisdiction1", null);
        LatLong coordinatePair = new LatLong(41.31742721517005, -72.93918211751856, j, 0);
        RemoteHost h = new RemoteHost("host1");
        h.setJurisdiction(j);
        j.getBounds().add(coordinatePair);
        j.getRemoteHosts().add(h);
        jurisdictionRepository.save(j);
        authLogin();

        HttpRequest<?> request = HttpRequest.GET("/config")
                .header("referer", "http://host1");
        HttpResponse<JurisdictionDTO> response = client.toBlocking()
                .exchange(request, JurisdictionDTO.class);
        JurisdictionDTO infoResponse = response.getBody().get();
        assertEquals(infoResponse.getJurisdictionId(), "1");
        assertEquals(infoResponse.getName(), "jurisdiction1");
        // from application-test.yml's property `micronaut.http.services.auth.urls`
        assertEquals("http://localhost:8080", infoResponse.getUnityAuthUrl());
        assertNotNull(infoResponse.getBounds());
        assertTrue(infoResponse.getBounds().length > 0);
    }

    private HttpResponse<?> createServiceRequest(String serviceCode, String address, Map attributes, String jurisdictionId) {
        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO(serviceCode);
        serviceRequestDTO.setgRecaptchaResponse("abc");
        serviceRequestDTO.setLongitude("43.3434");
        serviceRequestDTO.setLatitude("48.98");
        if (address != null) {
            serviceRequestDTO.setAddressString(address);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);
        payload.putAll(attributes);
        HttpRequest<?> request = HttpRequest.POST("/requests?jurisdiction_id="+jurisdictionId, payload)
                .header("Authorization", "Bearer token.text.here")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, Map.class);
    }

    private HttpResponse<?> createServiceRequest(PostRequestServiceRequestDTO serviceRequestDTO, Map attributes, String jurisdictionId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);
        payload.putAll(attributes);
        HttpRequest<?> request = HttpRequest.POST("/requests?jurisdiction_id="+jurisdictionId, payload)
                .header("Authorization", "Bearer token.text.here")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, Map.class);
    }
}