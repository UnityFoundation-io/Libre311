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
import app.dto.service.CreateServiceDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicerequest.*;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionInfoResponse;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdiction.RemoteHost;
import app.model.jurisdictionuser.JurisdictionUser;
import app.model.jurisdictionuser.JurisdictionUserRepository;
import app.model.service.ServiceRepository;
import app.model.service.servicedefinition.AttributeDataType;
import app.model.service.servicedefinition.AttributeValue;
import app.model.service.servicedefinition.ServiceDefinition;
import app.model.service.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.model.user.User;
import app.model.user.UserRepository;
import app.security.HasPermissionResponse;
import app.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments={"app-api-test-data"}, transactional = false)
public class RootControllerTest {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockReCaptchaService mockReCaptchaService;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    JurisdictionUserRepository jurisdictionUserRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    DbCleanup dbCleanup;

    @BeforeEach
    void setup() {
        dbCleanup.cleanupServiceRequests();
        mockAuthenticationFetcher.setAuthentication(null);
        setAuthHasPermissionSuccessResponse(false, null);

        String userEmail = "person1@test.io";
        Jurisdiction jurisdiction = jurisdictionRepository.findById("city.gov").get();
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        User user = userOptional.orElseGet(() -> userRepository.save(new User(userEmail)));
        jurisdictionUserRepository.save(new JurisdictionUser(user, jurisdiction, true));
    }

    private void setAuthHasPermissionSuccessResponse(boolean success, List<String> permissions) {
        if (success) {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(true, "person1@test.io", null, permissions)));
        } else {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(false, "person1@test.io", "Unauthorized", permissions)));
        }
    }

    void authLogin() {
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionSuccessResponse(true, null);
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
        ServiceDefinitionAttribute serviceDefinitionAttribute = serviceRequestDTO.getSelectedValues().get(0);
        assertNotNull(serviceDefinitionAttribute.getValues());
        assertFalse(serviceDefinitionAttribute.getValues().isEmpty());
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
    public void cannotCreateServiceRequestWithWithBlankJurisdiction() {
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
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
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


    // create service
    @Test
    public void canCreateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("BIKELN007", "Bike Lane Obstruction", "city.gov");
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // success, bare minimum
        response = createService("BIKELN007", "Bike Lane Obstruction", "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO[]> optional = response.getBody(ServiceDTO[].class);
        assertTrue(optional.isPresent());
        ServiceDTO[] postResponseServiceDTOS = optional.get();
        assertTrue(postResponseServiceDTOS.length > 0);

        // success, all provided
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setServiceCode("BUS_STOP");
        serviceDefinition.setAttributes(List.of(
                new ServiceDefinitionAttribute(
                        "ISSUE_NEAR",
                        true,
                        AttributeDataType.STRING,
                        false,
                        "Bus Stop Near",
                        1,
                        "(Optional) If the issue is near anything, please describe here.",
                        null
                ),
                new ServiceDefinitionAttribute(
                        "ISSUE_SELECT",
                        true,
                        AttributeDataType.MULTIVALUELIST,
                        true,
                        "Bus Stop Issues",
                        2,
                        "(Optional) If the issue is near anything, please describe here.",
                        List.of(
                                new AttributeValue("UNSAFE", "Unsafe location"),
                                new AttributeValue("NO_SDLWLK", "No Sidewalk present"),
                                new AttributeValue("MISSING_SIGN", "Sign is missing")
                        )
                )
        ));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(serviceDefinition);
        response = createService("BUS_STOP", "Bike Lane Obstruction", "Bike Lane", json, "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        optional = response.getBody(ServiceDTO[].class);
        assertTrue(optional.isPresent());
        postResponseServiceDTOS = optional.get();
        assertTrue(postResponseServiceDTOS.length > 0);
        ServiceDTO serviceDTO = postResponseServiceDTOS[0];
        assertNotNull(serviceDTO.getJurisdictionId());
        assertEquals("city.gov", serviceDTO.getJurisdictionId());

        // get service definition
        response = client.toBlocking().exchange("/services/"+serviceDTO.getServiceCode()+"?jurisdiction_id=city.gov", String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinitionResponse = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinitionResponse));
        ServiceDefinition serviceDefinitionObject = (new ObjectMapper()).readValue(serviceDefinitionResponse, ServiceDefinition.class);
        assertNotNull(serviceDefinitionObject.getServiceCode());
        assertNotNull(serviceDefinitionObject.getAttributes());
        assertFalse(serviceDefinitionObject.getAttributes().isEmpty());
        assertTrue(serviceDefinitionObject.getAttributes().stream()
                .anyMatch(serviceDefinitionAttribute ->
                        serviceDefinitionAttribute.getCode().equals("ISSUE_SELECT") &&
                                serviceDefinitionAttribute.getValues() != null &&
                                !serviceDefinitionAttribute.getValues().isEmpty())
        );

        // fail, jurisdiction not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("ROAD", "Road Issues", null);
        });
        assertEquals(NOT_FOUND, exception.getStatus());

        // fail, code not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService(null, "Road Issues", "city.gov");
        });
        assertEquals(BAD_REQUEST, exception.getStatus());

        // fail, name not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("ROAD", null, "city.gov");
        });
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void cannotCreateServiceForAnotherJurisdictionIfAuthenticated() {

        // Checks whether the user has the permission to create a service in their own jurisdiction, but does
        // not have a user-jurisdiction record locally.
        mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(true, "person2@test.com", null, List.of("LIBRE311_ADMIN_VIEW_SUBTENANT"))));

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("BIKELN010", "Bike Lane Obstruction", "town.gov");
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());
    }

    // update service
    @Test
    public void canUpdateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        // create
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setServiceCode("BUS_STOP_UPDATE");
        serviceDefinition.setAttributes(List.of(
                new ServiceDefinitionAttribute(
                        "ISSUE_NEAR",
                        true,
                        AttributeDataType.STRING,
                        false,
                        "Bus Stop Near",
                        1,
                        "(Optional) If the issue is near anything, please describe here.",
                        null
                ),
                new ServiceDefinitionAttribute(
                        "ISSUE_SELECT",
                        true,
                        AttributeDataType.MULTIVALUELIST,
                        true,
                        "Bus Stop Issues",
                        2,
                        "(Optional) If the issue is near anything, please describe here.",
                        List.of(
                                new AttributeValue("UNSAFE", "Unsafe location"),
                                new AttributeValue("NO_SDLWLK", "No Sidewalk present"),
                                new AttributeValue("MISSING_SIGN", "Sign is missing")
                        )
                )
        ));
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(serviceDefinition);
        response = createService("BUS_STOP_UPDATE", "Bus Stop Issues", "Issues pertaining to bus stops", json, "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO[]> optional = response.getBody(ServiceDTO[].class);
        assertTrue(optional.isPresent());
        ServiceDTO[] postResponseServiceDTOS = optional.get();
        assertTrue(postResponseServiceDTOS.length > 0);
        ServiceDTO serviceDTO = postResponseServiceDTOS[0];

        // update all
        UpdateServiceDTO updateServiceDTO = new UpdateServiceDTO();
        updateServiceDTO.setServiceCode("INNER_CITY_BUS_STOPS");
        updateServiceDTO.setServiceName("Inner City Bust Stops");
        updateServiceDTO.setDescription("Issues pertaining to inner city bus stops.");

        ServiceDefinition serviceDefinitionUpdate = new ServiceDefinition();
        serviceDefinitionUpdate.setServiceCode("INNER_CITY_BUS_STOPS");
        serviceDefinitionUpdate.setAttributes(List.of(
                new ServiceDefinitionAttribute(
                        "ISSUE_SELECT",
                        true,
                        AttributeDataType.MULTIVALUELIST,
                        true,
                        "Bus Stop Issues",
                        2,
                        "(Optional) If the issue is near anything, please describe here.",
                        List.of(
                                new AttributeValue("UNSAFE", "Unsafe location"),
                                new AttributeValue("NO_SDLWLK", "No Sidewalk present"),
                                new AttributeValue("MISSING_SIGN", "Sign is missing")
                        )
                )
        ));
        json = mapper.writeValueAsString(serviceDefinitionUpdate);
        updateServiceDTO.setServiceDefinitionJson(json);

        Map payload = mapper.convertValue(updateServiceDTO, Map.class);
        request = HttpRequest.PATCH("/admin/services/"+serviceDTO.getId()+"?jurisdiction_id=city.gov", payload)
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO[].class);
        assertEquals(OK, response.getStatus());

        // verify all
        Optional<ServiceDTO[]> body = response.getBody(ServiceDTO[].class);
        assertTrue(body.isPresent());
        ServiceDTO serviceDTO1 = body.get()[0];
        assertEquals("INNER_CITY_BUS_STOPS", serviceDTO1.getServiceCode());
        assertEquals("Inner City Bust Stops", serviceDTO1.getServiceName());
        assertEquals("Issues pertaining to inner city bus stops.", serviceDTO1.getDescription());

//        // get service definition
        response = client.toBlocking().exchange("/services/"+serviceDTO1.getServiceCode()+"?jurisdiction_id=city.gov", String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinitionResponse = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinitionResponse));
        ServiceDefinition serviceDefinitionObject = (new ObjectMapper()).readValue(serviceDefinitionResponse, ServiceDefinition.class);
        assertNotNull(serviceDefinitionObject.getServiceCode());
        assertEquals("INNER_CITY_BUS_STOPS", serviceDefinitionObject.getServiceCode());
        assertNotNull(serviceDefinitionObject.getAttributes());
        assertFalse(serviceDefinitionObject.getAttributes().isEmpty());
        assertEquals(1, serviceDefinitionObject.getAttributes().size());
        assertTrue(serviceDefinitionObject.getAttributes().stream()
                .anyMatch(serviceDefinitionAttribute ->
                        serviceDefinitionAttribute.getCode().equals("ISSUE_SELECT") &&
                                serviceDefinitionAttribute.getValues() != null &&
                                !serviceDefinitionAttribute.getValues().isEmpty())
        );
    }

    // update
    @Test
    public void canUpdateServiceRequestIfAuthenticated() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "CRACKED"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();
        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];

        // update attempt
        PatchServiceRequestDTO patchServiceRequestDTO = new PatchServiceRequestDTO();
        patchServiceRequestDTO.setPriority(ServiceRequestPriority.HIGH);
        patchServiceRequestDTO.setStatus(ServiceRequestStatus.IN_PROGRESS);
        patchServiceRequestDTO.setServiceNotice("To be fulfilled by Acme Concrete Co.");
        patchServiceRequestDTO.setAgencyEmail("acme@example.com");
        patchServiceRequestDTO.setAgencyResponsible("Acme Concrete");
        patchServiceRequestDTO.setStatusNotes("Will investigate and remediate within 2 weeks");

        Map payload = (new ObjectMapper()).convertValue(patchServiceRequestDTO, Map.class);
        HttpRequest<?> request = HttpRequest
                .PATCH("/admin/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=city.gov", payload)
                .header("Authorization", "Bearer token.text.here");

        HttpRequest<?> finalRequest = request;
        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(finalRequest, SensitiveServiceRequestDTO[].class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // update
        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<SensitiveServiceRequestDTO[]> bodyOptional = response.getBody(SensitiveServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        SensitiveServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
        SensitiveServiceRequestDTO updatedServiceRequestDTO = serviceRequestDTOS[0];
        assertEquals("acme@example.com", updatedServiceRequestDTO.getAgencyEmail());
        assertEquals("To be fulfilled by Acme Concrete Co.", updatedServiceRequestDTO.getServiceNotice());
        assertEquals("Acme Concrete", updatedServiceRequestDTO.getAgencyResponsible());
        assertEquals("Will investigate and remediate within 2 weeks", updatedServiceRequestDTO.getStatusNotes());
        assertEquals(ServiceRequestPriority.HIGH, updatedServiceRequestDTO.getPriority());
        assertEquals(ServiceRequestStatus.IN_PROGRESS, updatedServiceRequestDTO.getStatus());

        // update dates
        request = HttpRequest
                .PATCH("admin/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=city.gov",
                        Map.of(
                                "jurisdiction_id", "city.gov",
                                "closed_date", "2023-01-25T13:15:30Z",
                                "expected_date", "2023-01-15T13:15:30Z"
                        ))
                .header("Authorization", "Bearer token.text.here");

        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());
        bodyOptional = response.getBody(SensitiveServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
        updatedServiceRequestDTO = serviceRequestDTOS[0];
        assertNotNull(updatedServiceRequestDTO.getClosedDate());
        assertNotNull(updatedServiceRequestDTO.getExpectedDate());
    }

    // read
    @Test
    public void canReadServiceRequestSensitiveInfoIfAuthenticated() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(PostResponseServiceRequestDTO[].class);
        assertTrue(optional.isPresent());
        PostResponseServiceRequestDTO[] postResponseServiceRequestDTOS = optional.get();

        PostResponseServiceRequestDTO postResponseServiceRequestDTO = postResponseServiceRequestDTOS[0];

        // unauthenticated read attempt
        HttpRequest<?> request = HttpRequest.GET("/admin/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=city.gov")
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, SensitiveServiceRequestDTO[].class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO[].class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<SensitiveServiceRequestDTO[]> bodyOptional = response.getBody(SensitiveServiceRequestDTO[].class);
        assertTrue(bodyOptional.isPresent());
        SensitiveServiceRequestDTO[] serviceRequestDTOS = bodyOptional.get();
        assertTrue(Arrays.stream(serviceRequestDTOS).findAny().isPresent());
        assertEquals(1, serviceRequestDTOS.length);
    }


    private HttpResponse<?> createService(String code, String name, String jurisdictionId) {
        return createService(code, name, null, null, jurisdictionId);
    }

    private HttpResponse<?> createService(String code, String name, String description, String serviceDefinitionJson, String jurisdictionId) {
        CreateServiceDTO serviceDTO = new CreateServiceDTO();
        serviceDTO.setServiceCode(code);
        serviceDTO.setServiceName(name);
        serviceDTO.setDescription(description);
        serviceDTO.setServiceDefinitionJson(serviceDefinitionJson);
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceDTO, Map.class);
        HttpRequest<?> request = HttpRequest.POST("/admin/services?jurisdiction_id="+jurisdictionId, payload)
                .header("Authorization", "Bearer token.text.here")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, ServiceDTO[].class);
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
    public void canDownloadCSVFile() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("006", "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "NARROW"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        // create service requests
        HttpRequest<?> request = HttpRequest.GET("/admin/requests/download?jurisdiction_id=city.gov")
                .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(request, byte[].class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());
        InputStream inputStream = new ByteArrayInputStream(body.get());
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertNotNull(text);
    }

    @Test
    public void theCSVFileShouldNotContainCellsBeginningWithUnsafeCharacters() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("001", "=1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "@1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "+1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "-1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "\t1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "\r1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        // create service requests
        HttpRequest<?> request = HttpRequest.GET("admin/requests/download?jurisdiction_id=city.gov")
                .header("Authorization", "Bearer token.text.here");

        authLogin();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());
        InputStream inputStream = new ByteArrayInputStream(body.get());
        String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertNotNull(text);
        try (CSVReader reader = new CSVReader(new StringReader(text))) {
            reader.skip(1); // skip header
            String[] cells = reader.readNext();
            while (cells != null) {
                String addressCell = cells[0];
                assertTrue(addressCell.startsWith("'"));
                cells = reader.readNext();
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getJurisdictionTest() {
        RemoteHost h = new RemoteHost("host1");
        Jurisdiction j = new Jurisdiction("1", 1L, "jurisdiction1", null);
        h.setJurisdiction(j);
        j.getRemoteHosts().add(h);
        jurisdictionRepository.save(j);
        authLogin();

        HttpRequest<?> request = HttpRequest.GET("/config")
                .header("host", "host1");
        HttpResponse<JurisdictionInfoResponse> response = client.toBlocking()
                .exchange(request, JurisdictionInfoResponse.class);
        JurisdictionInfoResponse infoResponse = response.getBody().get();
        assertEquals(infoResponse.getId(), "1");
        assertEquals(infoResponse.getName(), "jurisdiction1");
        // from application-test.yml's property `micronaut.http.services.auth.urls`
        assertEquals("http://localhost:8080", infoResponse.getUnityAuthUrl());
    }

    private HttpResponse<?> createService(String code, String name) {
        return createService(code, name, null, null);
    }

    private HttpResponse<?> createService(String code, String name, String description, String serviceDefinitionJson) {
        CreateServiceDTO serviceDTO = new CreateServiceDTO();
        serviceDTO.setServiceCode(code);
        serviceDTO.setServiceName(name);
        serviceDTO.setDescription(description);
        serviceDTO.setServiceDefinitionJson(serviceDefinitionJson);
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceDTO, Map.class);
        HttpRequest<?> request = HttpRequest.POST("/admin/services", payload)
                .header("Authorization", "Bearer token.text.here")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, ServiceDTO[].class);
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