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
import app.fixture.JurisdictionBoundaryRepositoryFixture;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.RemoteHost;
import app.model.jurisdictionuser.JurisdictionUser;
import app.model.service.AttributeDataType;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.dto.servicedefinition.ServiceDefinitionAttributeDTO;
import app.model.service.ServiceType;
import app.model.service.group.ServiceGroup;
import app.model.servicedefinition.AttributeValue;
import app.model.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.model.user.User;
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
import org.junit.jupiter.api.AfterEach;
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

@MicronautTest(transactional = false)
public class RootControllerTest extends JurisdictionBoundaryRepositoryFixture {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

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
        setupMockData();
    }

    @AfterEach
    void teardown(){
        dbCleanup.cleanup();
    }

    void setupTownJurisdiction(){
        Jurisdiction jurisdiction = jurisdictionRepository.save(
            new Jurisdiction("town.gov", 2L));
        saveJurisdictionBoundary(jurisdiction, DEFAULT_BOUNDS);
        ServiceGroup infrastructureGroup = dbCleanup.serviceGroupRepository.save(new ServiceGroup("Infrastructure", jurisdiction));
        ServiceGroup unknownGroup = dbCleanup.serviceGroupRepository.save(new ServiceGroup("Unknown", jurisdiction));

        // save bus stop service and its attributes
        Service busStopService = dbCleanup.serviceRepository.save(new Service("Bus stop"));
        busStopService.setServiceCode("BUS_STOP");
        busStopService.setType(ServiceType.REALTIME);
        busStopService.setJurisdiction(jurisdiction);
        busStopService.setServiceGroup(infrastructureGroup);
        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(busStopService);
        serviceDefinitionAttribute.setCode("BUS_STOP");
        serviceDefinitionAttribute.setVariable(true);
        serviceDefinitionAttribute.setDatatype(AttributeDataType.MULTIVALUELIST);
        serviceDefinitionAttribute.setRequired(false);
        serviceDefinitionAttribute.setDescription(
            "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        serviceDefinitionAttribute.setAttributeOrder(1);
        serviceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");
        ServiceDefinitionAttribute savedSDA = dbCleanup.serviceDefinitionAttributeRepository.save(
            serviceDefinitionAttribute);
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "ADA Access"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Cracked"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Too narrow"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Heaved/Uneven Sidewalk"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Other"));


        // save crosswalk service and its attributes
        Service crosswalkService = new Service("crosswalk service");
        crosswalkService.setServiceCode("CRSWLK");
        crosswalkService.setType(ServiceType.REALTIME);
        crosswalkService.setJurisdiction(jurisdiction);
        crosswalkService.setServiceGroup(infrastructureGroup);
        crosswalkService = dbCleanup.serviceRepository.save(crosswalkService);

        ServiceDefinitionAttribute crosswalkSDA = new ServiceDefinitionAttribute();
        crosswalkSDA.setService(crosswalkService);
        crosswalkSDA.setCode("BUS_STOP");
        crosswalkSDA.setVariable(true);
        crosswalkSDA.setDatatype(AttributeDataType.MULTIVALUELIST);
        crosswalkSDA.setRequired(false);
        crosswalkSDA.setDescription(
            "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        crosswalkSDA.setAttributeOrder(1);
        crosswalkSDA.setDatatypeDescription("Please select one or more items.");
        crosswalkSDA = dbCleanup.serviceDefinitionAttributeRepository.save(serviceDefinitionAttribute);
        dbCleanup.attributeValueRepository.save(new AttributeValue(crosswalkSDA, "ADA Access"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(crosswalkSDA, "Cracked"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(crosswalkSDA, "Too narrow"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(crosswalkSDA, "Heaved/Uneven Sidewalk"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(crosswalkSDA, "Other"));

    }



    public void setupMockData() {
        setupTownJurisdiction();
        User admin = dbCleanup.userRepository.save(new User("admin@fakecity.gov"));
        User user = dbCleanup.userRepository.save(new User("user@fakecity.gov"));
        Jurisdiction jurisdiction = jurisdictionRepository.save(
            new Jurisdiction("city.gov", 1L));
        saveJurisdictionBoundary(jurisdiction, DEFAULT_BOUNDS);
        dbCleanup.jurisdictionUserRepository.save(new JurisdictionUser(admin, jurisdiction, true));
        dbCleanup.jurisdictionUserRepository.save(new JurisdictionUser(user, jurisdiction, false));

        // service
        ServiceGroup infrastructureGroup = dbCleanup.serviceGroupRepository.save(
            new ServiceGroup("Infrastructure", jurisdiction));
        Service sidewalkService = new Service("Sidewalk");
        sidewalkService.setServiceCode("001");
        sidewalkService.setType(ServiceType.REALTIME);
        sidewalkService.setJurisdiction(jurisdiction);
        sidewalkService.setServiceGroup(infrastructureGroup);
        Service service = serviceRepository.save(sidewalkService);

        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(service);
        serviceDefinitionAttribute.setCode("SDWLK");
        serviceDefinitionAttribute.setVariable(true);
        serviceDefinitionAttribute.setDatatype(AttributeDataType.MULTIVALUELIST);
        serviceDefinitionAttribute.setRequired(false);
        serviceDefinitionAttribute.setDescription(
            "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        serviceDefinitionAttribute.setAttributeOrder(1);
        serviceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");

        ServiceDefinitionAttribute savedSDA = dbCleanup.serviceDefinitionAttributeRepository.save(
            serviceDefinitionAttribute);

        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "ADA Access"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Cracked"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Too narrow"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Heaved/Uneven Sidewalk"));
        dbCleanup.attributeValueRepository.save(new AttributeValue(savedSDA, "Other"));


        Service bikeLaneService = new Service("Bike lane service");
        bikeLaneService.setServiceCode("003");
        bikeLaneService.setType(ServiceType.REALTIME);
        bikeLaneService.setJurisdiction(jurisdiction);
        bikeLaneService.setServiceGroup(infrastructureGroup);
        serviceRepository.save(bikeLaneService);

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
        assertEquals(BAD_REQUEST, thrown.getStatus());
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
            createServiceRequest(serviceRequestDTO, Map.of(), "city.gov");
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
        response = createServiceRequest("006", "12345 Fairway", Map.of(), "city.gov");
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
        serviceRequestDTO.setLongitude(String.valueOf(IN_BOUNDS_COORDINATE.getX()));
        serviceRequestDTO.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
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

        Optional<Service> optionalSidewalkService = serviceRepository.findByServiceCodeAndJurisdictionId("001", "city.gov");
        assertTrue(optionalSidewalkService.isPresent());
        Service sidewalkService = optionalSidewalkService.get();

        Optional<Service> optionalBikeLaneService = serviceRepository.findByServiceCodeAndJurisdictionId("003", "city.gov");
        assertTrue(optionalBikeLaneService.isPresent());
        Service bikeLaneService = optionalBikeLaneService.get();

        ServiceRequest closedHighPriority = new ServiceRequest();
        closedHighPriority.setStatus(ServiceRequestStatus.CLOSED);
        closedHighPriority.setPriority(ServiceRequestPriority.HIGH);
        closedHighPriority.setService(sidewalkService);
        closedHighPriority.setJurisdiction(sidewalkService.getJurisdiction());
        closedHighPriority.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        closedHighPriority.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        ServiceRequest closedHighSR = serviceRequestRepository.save(closedHighPriority);

        ServiceRequest openLowPriority = new ServiceRequest();
        openLowPriority.setStatus(ServiceRequestStatus.OPEN);
        openLowPriority.setPriority(ServiceRequestPriority.LOW);
        openLowPriority.setService(sidewalkService);
        openLowPriority.setJurisdiction(sidewalkService.getJurisdiction());
        openLowPriority.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        openLowPriority.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        ServiceRequest openLowSR = serviceRequestRepository.save(openLowPriority);

        ServiceRequest assignedMedium = new ServiceRequest();
        assignedMedium.setStatus(ServiceRequestStatus.ASSIGNED);
        assignedMedium.setPriority(ServiceRequestPriority.MEDIUM);
        assignedMedium.setService(sidewalkService);
        assignedMedium.setJurisdiction(sidewalkService.getJurisdiction());
        assignedMedium.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        assignedMedium.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        serviceRequestRepository.save(assignedMedium);

        ServiceRequest bikeLaneRequest = new ServiceRequest();
        bikeLaneRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        bikeLaneRequest.setService(bikeLaneService);
        bikeLaneRequest.setJurisdiction(bikeLaneService.getJurisdiction());
        bikeLaneRequest.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        bikeLaneRequest.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        ServiceRequest bikeLaneSR = serviceRequestRepository.save(bikeLaneRequest);

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

        // filter service_code 003 and 001
        req = HttpRequest.GET("/requests?jurisdiction_id=city.gov&service_code=003&service_code=001").bearerAuth( "eyekljdsl");
        response = client.toBlocking().exchange(req,
                Argument.listOf(SensitiveServiceRequestDTO.class));
        assertEquals(HttpStatus.OK, response.status());
        body = response.getBody(Argument.listOf(SensitiveServiceRequestDTO.class));
        assertTrue(body.isPresent());
        assertFalse(body.get().isEmpty());
        assertTrue(body.get().stream().anyMatch(sensitiveServiceRequestDTO ->
                sensitiveServiceRequestDTO.getId().equals(bikeLaneSR.getId()) ||
                        sensitiveServiceRequestDTO.getId().equals(closedHighSR.getId())));
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

        RemoteHost h = new RemoteHost("host1");
        h.setJurisdiction(j);
        j.getRemoteHosts().add(h);
        jurisdictionRepository.save(j);
        saveJurisdictionBoundary(j, DEFAULT_BOUNDS);
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
        serviceRequestDTO.setLongitude(String.valueOf(IN_BOUNDS_COORDINATE.getX()));
        serviceRequestDTO.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
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