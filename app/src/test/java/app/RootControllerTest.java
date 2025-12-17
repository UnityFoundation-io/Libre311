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

import app.discovery.Discovery;
import app.jurisdiction.JurisdictionDTO;
import app.servicedefinition.ServiceDTO;
import app.servicedefinition.ServiceDefinition;
import app.servicerequest.ServiceRequestPostRequest;
import app.servicerequest.ServiceRequestPostResponse;
import app.servicerequest.SensitiveServiceRequestDTO;
import app.servicerequest.ServiceRequestDTO;
import app.jurisdiction.Jurisdiction;
import app.jurisdiction.JurisdictionRepository;

import app.jurisdiction.RemoteHost;
import app.servicedefinition.AttributeDataType;
import app.servicedefinition.ServiceRepository;
import app.servicedefinition.ServiceDefinitionAttributeDTO;
import app.servicedefinition.ServiceType;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.servicedefinition.AttributeValue;
import app.servicedefinition.AttributeValueRepository;
import app.servicedefinition.ServiceDefinitionAttribute;
import app.servicedefinition.ServiceDefinitionAttributeRepository;
import app.servicerequest.ServiceRequest;
import app.servicerequest.ServiceRequestPriority;
import app.servicerequest.ServiceRequestRepository;
import app.servicerequest.ServiceRequestStatus;
import app.security.HasPermissionResponse;
import app.security.Permission;
import app.geometry.LibreGeometryFactory;
import app.jurisdiction.JurisdictionBoundaryService;
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

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.*;

import org.locationtech.jts.geom.Coordinate;

import static app.util.JurisdictionBoundaryUtil.DEFAULT_BOUNDS;
import static app.util.JurisdictionBoundaryUtil.IN_BOUNDS_COORDINATE;
import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceDefinitionAttributeRepository serviceDefinitionAttributeRepository;

    @Inject
    AttributeValueRepository attributeValueRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    JurisdictionBoundaryService jurisdictionBoundaryService;

    @Inject
    DbCleanup dbCleanup;

    @Inject
    LibreGeometryFactory libreGeometryFactory;

    private ServiceDefinition sidewalkServiceDefinition;
    private ServiceDefinition bikeLaneServiceDefinition;
    private ServiceDefinition townOtherServiceDefinition;
    private ServiceDefinitionAttribute sidewalkMultiValueAttr;
    private ServiceDefinitionAttribute sdwlkString;
    private ServiceDefinitionAttribute sdwlkNumber;
    private ServiceDefinitionAttribute sdwlkDatetime;
    private ServiceDefinitionAttribute sdwlkText;
    private ServiceDefinitionAttribute sdwlkSingleValueList;
    private ServiceDefinitionAttribute bkLnSavedSDA;

    @BeforeEach
    void setup() {
        dbCleanup.cleanupServiceRequests();

        setupCityJurisdiction();
        setupTownJurisdiction();
    }

    @AfterEach
    void teardown(){
        dbCleanup.cleanupAll();
    }

    private void setupCityJurisdiction() {
        // Jurisdiction
        Jurisdiction city = jurisdictionRepository.save(new Jurisdiction("city.gov", 1L));
        jurisdictionBoundaryService.saveBoundary(city, DEFAULT_BOUNDS);

        // Service Group
        ServiceGroup infrastructureGroup = serviceGroupRepository.save(
                new ServiceGroup("Infrastructure", city));

        setupSidewalkServiceDefinition(city, infrastructureGroup);
        setupBikeLaneServiceDefinition(city, infrastructureGroup);
    }

    private void setupSidewalkServiceDefinition(Jurisdiction city, ServiceGroup infrastructureGroup) {
        // Sidewalk Service
        ServiceDefinition newSidewalkServiceDefinition = new ServiceDefinition("Sidewalk");
        newSidewalkServiceDefinition.setType(ServiceType.REALTIME);
        newSidewalkServiceDefinition.setJurisdiction(city);
        newSidewalkServiceDefinition.setServiceGroup(infrastructureGroup);
        sidewalkServiceDefinition = serviceRepository.save(newSidewalkServiceDefinition);

        // Sidewalk Service Definitions
        ServiceDefinitionAttribute sidewalkMultiValueAttr = new ServiceDefinitionAttribute();
        sidewalkMultiValueAttr.setService(sidewalkServiceDefinition);
        sidewalkMultiValueAttr.setVariable(true);
        sidewalkMultiValueAttr.setDatatype(AttributeDataType.MULTIVALUELIST);
        sidewalkMultiValueAttr.setRequired(true);
        sidewalkMultiValueAttr.setDescription(
                "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        sidewalkMultiValueAttr.setAttributeOrder(1);
        sidewalkMultiValueAttr.setDatatypeDescription("Please select one or more items.");

        this.sidewalkMultiValueAttr = serviceDefinitionAttributeRepository.save(sidewalkMultiValueAttr);
        addValuesToAttribute(this.sidewalkMultiValueAttr, Set.of("ADA Access", "Cracked", "Too narrow", "Heaved/Uneven Sidewalk", "Other"));


        ServiceDefinitionAttribute sdwlkString = new ServiceDefinitionAttribute();
        sdwlkString.setService(sidewalkServiceDefinition);
        sdwlkString.setVariable(true);
        sdwlkString.setDatatype(AttributeDataType.STRING);
        sdwlkString.setRequired(false);
        sdwlkString.setDescription("Please add additional information.");
        sdwlkString.setAttributeOrder(2);
        sdwlkString.setDatatypeDescription("Please add additional information.");

        this.sdwlkString = serviceDefinitionAttributeRepository.save(sdwlkString);


        ServiceDefinitionAttribute sdwlkNumber = new ServiceDefinitionAttribute();
        sdwlkNumber.setService(sidewalkServiceDefinition);
        sdwlkNumber.setVariable(true);
        sdwlkNumber.setDatatype(AttributeDataType.NUMBER);
        sdwlkNumber.setRequired(false);
        sdwlkNumber.setDescription("In feet, how large is the issue?");
        sdwlkNumber.setAttributeOrder(3);
        sdwlkNumber.setDatatypeDescription("In feet, how large is the issue?");

        this.sdwlkNumber = serviceDefinitionAttributeRepository.save(sdwlkNumber);


        ServiceDefinitionAttribute sdwlkDatetime = new ServiceDefinitionAttribute();
        sdwlkDatetime.setService(sidewalkServiceDefinition);
        sdwlkDatetime.setVariable(true);
        sdwlkDatetime.setDatatype(AttributeDataType.DATETIME);
        sdwlkDatetime.setRequired(false);
        sdwlkDatetime.setDescription("When was the issue observed?");
        sdwlkDatetime.setAttributeOrder(4);
        sdwlkDatetime.setDatatypeDescription("When was the issue observed?");

        this.sdwlkDatetime = serviceDefinitionAttributeRepository.save(sdwlkDatetime);


        ServiceDefinitionAttribute sdwlkText = new ServiceDefinitionAttribute();
        sdwlkText.setService(sidewalkServiceDefinition);
        sdwlkText.setVariable(true);
        sdwlkText.setDatatype(AttributeDataType.TEXT);
        sdwlkText.setRequired(false);
        sdwlkText.setDescription("Any additional comments?");
        sdwlkText.setAttributeOrder(5);
        sdwlkText.setDatatypeDescription("Any additional comments?");

        this.sdwlkText = serviceDefinitionAttributeRepository.save(sdwlkText);


        ServiceDefinitionAttribute sdwlkSingleValueList = new ServiceDefinitionAttribute();
        sdwlkSingleValueList.setService(sidewalkServiceDefinition);
        sdwlkSingleValueList.setVariable(true);
        sdwlkSingleValueList.setDatatype(AttributeDataType.SINGLEVALUELIST);
        sdwlkSingleValueList.setRequired(false);
        sdwlkSingleValueList.setDescription("Please select a value.");
        sdwlkSingleValueList.setAttributeOrder(6);
        sdwlkSingleValueList.setDatatypeDescription("Please select a value.");

        this.sdwlkSingleValueList = serviceDefinitionAttributeRepository.save(sdwlkSingleValueList);
        addValuesToAttribute(this.sdwlkSingleValueList, Set.of("ADA Access", "Cracked", "Too narrow", "Heaved/Uneven Sidewalk", "Other"));
    }

    private void setupBikeLaneServiceDefinition(Jurisdiction city, ServiceGroup infrastructureGroup) {
        // Bike Lane Service
        ServiceDefinition newBikeLaneServiceDefinition = new ServiceDefinition("Bike Lane");
        newBikeLaneServiceDefinition.setType(ServiceType.REALTIME);
        newBikeLaneServiceDefinition.setJurisdiction(city);
        newBikeLaneServiceDefinition.setServiceGroup(infrastructureGroup);
        bikeLaneServiceDefinition = serviceRepository.save(newBikeLaneServiceDefinition);

        // Bike Lane Service Definition
        ServiceDefinitionAttribute bikeLaneServiceDefinitionAttribute = new ServiceDefinitionAttribute();
        bikeLaneServiceDefinitionAttribute.setService(bikeLaneServiceDefinition);
        bikeLaneServiceDefinitionAttribute.setVariable(true);
        bikeLaneServiceDefinitionAttribute.setDatatype(AttributeDataType.MULTIVALUELIST);
        bikeLaneServiceDefinitionAttribute.setRequired(true);
        bikeLaneServiceDefinitionAttribute.setDescription(
                "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        bikeLaneServiceDefinitionAttribute.setAttributeOrder(1);
        bikeLaneServiceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");

        this.bkLnSavedSDA = serviceDefinitionAttributeRepository.save(bikeLaneServiceDefinitionAttribute);
        addValuesToAttribute(this.bkLnSavedSDA, Set.of("ADA Access", "Incomplete", "Uneven", "Unsafe location", "Other"));
    }

    private void addValuesToAttribute(ServiceDefinitionAttribute serviceDefinitionAttribute, Set<String> values) {
        values.forEach(s -> serviceDefinitionAttribute.addAttributeValue(
                attributeValueRepository.save(new AttributeValue(sidewalkMultiValueAttr, s))
        ));
    }

    private Long getAttributeValueId(ServiceDefinitionAttribute serviceDefinitionAttribute, String valueName) {
        return serviceDefinitionAttribute.getAttributeValues().stream()
                .filter(attributeValue -> attributeValue.getValueName().equalsIgnoreCase(valueName))
                .findFirst().get().getId();
    }

    private void setupTownJurisdiction() {
        Jurisdiction town = jurisdictionRepository.save(new Jurisdiction("town.gov", 2L));
        jurisdictionBoundaryService.saveBoundary(town, DEFAULT_BOUNDS);

        ServiceGroup unknownGroup = serviceGroupRepository.save(
                new ServiceGroup("Unknown", town));

        // Sidewalk Service
        ServiceDefinition newSidewalkServiceDefinition = new ServiceDefinition("Other");
        newSidewalkServiceDefinition.setType(ServiceType.REALTIME);
        newSidewalkServiceDefinition.setJurisdiction(town);
        newSidewalkServiceDefinition.setServiceGroup(unknownGroup);
        townOtherServiceDefinition = serviceRepository.save(newSidewalkServiceDefinition);
    }

    @AfterAll
    void cleanupAll(){
        dbCleanup.cleanupAll();
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

        response = createServiceRequest(townOtherServiceDefinition.getId(), "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceRequestPostResponse[]> optional = response.getBody(ServiceRequestPostResponse[].class);
        assertTrue(optional.isPresent());
        ServiceRequestPostResponse[] serviceRequestPostResponses = optional.get();
        assertTrue(Arrays.stream(serviceRequestPostResponses)
            .anyMatch(serviceRequestPostResponse -> serviceRequestPostResponse.getId() != null));
    }

    @Test
    public void canCreateServiceRequestWithRequiredAttributes() {
        HttpResponse<?> response;

        response = createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway",
                Map.of("attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow")),
                "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceRequestPostResponse[]> optional = response.getBody(ServiceRequestPostResponse[].class);
        assertTrue(optional.isPresent());
        ServiceRequestPostResponse[] serviceRequestPostResponses = optional.get();
        assertTrue(Arrays.stream(serviceRequestPostResponses)
            .anyMatch(serviceRequestPostResponse -> serviceRequestPostResponse.getId() != null));
    }

    @Test
    public void canCreateServiceRequestWithVaryingDatatypes() {
        HttpResponse<?> response;

        response = createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway",
            Map.of(
                "attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow"),
                "attribute["+sdwlkString.getId()+"]", "A string description",
                "attribute["+sdwlkNumber.getId()+"]", "5",
                "attribute["+sdwlkDatetime.getId()+"]", "2015-04-14T11:07:36.639Z",
                "attribute["+sdwlkText.getId()+"]", "This is a comment field that can introduce multiline characters",
                "attribute["+sdwlkSingleValueList.getId()+"]", getAttributeValueId(sdwlkSingleValueList, "Too narrow")
            ),
            "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceRequestPostResponse[]> optional = response.getBody(ServiceRequestPostResponse[].class);
        assertTrue(optional.isPresent());
        ServiceRequestPostResponse[] serviceRequestPostResponses = optional.get();
        assertTrue(Arrays.stream(serviceRequestPostResponses)
            .anyMatch(serviceRequestPostResponse -> serviceRequestPostResponse.getId() != null));

        ServiceRequestPostResponse serviceRequestPostResponse = serviceRequestPostResponses[0];


        // GET
        response = client.toBlocking().exchange("/requests/" + serviceRequestPostResponse.getId() + "?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
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
            createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway",
                Map.of(
                    "attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow"),
                    "attribute["+sdwlkDatetime.getId()+"]", "0015/04/14Z"
                ),
                "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithInvalidFormattedNumberField() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway",
                Map.of(
                    "attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow"),
                    "attribute["+sdwlkNumber.getId()+"]", "NotANumber"
                ),
                "city.gov");
        });
        assertEquals(INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithoutRequiredAttributes() {
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
            createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway", Map.of(), "city.gov");
        });
        assertEquals(BAD_REQUEST, thrown.getStatus());
    }

    @Test
    public void cannotCreateServiceRequestWithBlankJurisdiction() {
        ServiceRequestPostRequest serviceRequestDTO = new ServiceRequestPostRequest(townOtherServiceDefinition.getId());
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
        ServiceRequestPostRequest serviceRequestDTO = new ServiceRequestPostRequest(townOtherServiceDefinition.getId());
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

        response = client.toBlocking().exchange("/services/"+ sidewalkServiceDefinition.getId()+"?jurisdiction_id=city.gov", String.class);
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
        response = createServiceRequest(townOtherServiceDefinition.getId(), "12345 Fairway", Map.of(), "town.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest(sidewalkServiceDefinition.getId(), "12345 Fairway",
            Map.of("attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow")), "city.gov");
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

        ServiceRequestPostRequest serviceRequestDTO = new ServiceRequestPostRequest(sidewalkServiceDefinition.getId());
        serviceRequestDTO.setgRecaptchaResponse("abc");
        serviceRequestDTO.setLongitude(String.valueOf(IN_BOUNDS_COORDINATE.getX()));
        serviceRequestDTO.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        serviceRequestDTO.setEmail("private@test.com");

        createServiceRequest(serviceRequestDTO,
                Map.of("attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow")),
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

        ServiceRequest closedHighPriority = new ServiceRequest();
        closedHighPriority.setStatus(ServiceRequestStatus.CLOSED);
        closedHighPriority.setPriority(ServiceRequestPriority.HIGH);
        closedHighPriority.setService(sidewalkServiceDefinition);
        closedHighPriority.setJurisdiction(sidewalkServiceDefinition.getJurisdiction());
        setLocation(closedHighPriority, IN_BOUNDS_COORDINATE);
        ServiceRequest closedHighSR = serviceRequestRepository.save(closedHighPriority);

        ServiceRequest openLowPriority = new ServiceRequest();
        openLowPriority.setStatus(ServiceRequestStatus.OPEN);
        openLowPriority.setPriority(ServiceRequestPriority.LOW);
        openLowPriority.setService(sidewalkServiceDefinition);
        openLowPriority.setJurisdiction(sidewalkServiceDefinition.getJurisdiction());
        setLocation(openLowPriority, IN_BOUNDS_COORDINATE);
        ServiceRequest openLowSR = serviceRequestRepository.save(openLowPriority);

        ServiceRequest assignedMedium = new ServiceRequest();
        assignedMedium.setStatus(ServiceRequestStatus.ASSIGNED);
        assignedMedium.setPriority(ServiceRequestPriority.MEDIUM);
        assignedMedium.setService(sidewalkServiceDefinition);
        assignedMedium.setJurisdiction(sidewalkServiceDefinition.getJurisdiction());
        setLocation(assignedMedium, IN_BOUNDS_COORDINATE);
        serviceRequestRepository.save(assignedMedium);

        ServiceRequest bikeLaneRequest = new ServiceRequest();
        bikeLaneRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        bikeLaneRequest.setService(bikeLaneServiceDefinition);
        bikeLaneRequest.setJurisdiction(bikeLaneServiceDefinition.getJurisdiction());
        setLocation(bikeLaneRequest, IN_BOUNDS_COORDINATE);
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

        // filter service_codes for bike lane and sidewalk
        req = HttpRequest.GET("/requests?jurisdiction_id=city.gov&service_code="+ bikeLaneServiceDefinition.getId()+"&service_code="+ sidewalkServiceDefinition.getId()).bearerAuth( "eyekljdsl");
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
        response = createServiceRequest(sidewalkMultiValueAttr.getService().getId(), "12345 Fairway",
            Map.of("attribute["+sidewalkMultiValueAttr.getId()+"]", getAttributeValueId(sidewalkMultiValueAttr, "Too narrow")), "city.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceRequestPostResponse[]> optional = response.getBody(ServiceRequestPostResponse[].class);
        assertTrue(optional.isPresent());
        ServiceRequestPostResponse[] serviceRequestPostResponses = optional.get();
        ServiceRequestPostResponse serviceRequestPostResponse = serviceRequestPostResponses[0];

        response = client.toBlocking().exchange("/requests/" + serviceRequestPostResponse.getId()+"?jurisdiction_id=city.gov", ServiceRequestDTO[].class);
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

        Optional<Discovery> bodyOptional = response.getBody(Discovery.class);
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
//        LatLong coordinatePair = new LatLong(41.31742721517005, -72.93918211751856, j, 0);
        RemoteHost h = new RemoteHost("host1");
        h.setJurisdiction(j);

        j.getRemoteHosts().add(h);
        j = jurisdictionRepository.save(j);
        jurisdictionBoundaryService.saveBoundary(j, DEFAULT_BOUNDS);
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

    @Test
    public void getJurisdictionReturnsDefaultPolicyContentWhenNotSet() {
        Jurisdiction j = new Jurisdiction("policy-test-1", 1L, "Policy Test Jurisdiction", null);
        RemoteHost h = new RemoteHost("policyhost1");
        h.setJurisdiction(j);
        j.getRemoteHosts().add(h);
        // Explicitly leave policy content null
        j.setPrivacyPolicyContent(null);
        j.setTermsOfUseContent(null);
        j = jurisdictionRepository.save(j);
        jurisdictionBoundaryService.saveBoundary(j, DEFAULT_BOUNDS);

        HttpRequest<?> request = HttpRequest.GET("/config")
            .header("referer", "http://policyhost1");
        HttpResponse<JurisdictionDTO> response = client.toBlocking()
            .exchange(request, JurisdictionDTO.class);
        JurisdictionDTO infoResponse = response.getBody().get();

        // Should return default content, not null
        assertNotNull(infoResponse.getPrivacyPolicyContent());
        assertNotNull(infoResponse.getTermsOfUseContent());
        // Verify it's the default content by checking for expected text
        assertTrue(infoResponse.getPrivacyPolicyContent().contains("Privacy Policy"));
        assertTrue(infoResponse.getTermsOfUseContent().contains("Terms of Use"));
    }

    @Test
    public void getJurisdictionReturnsCustomPolicyContentWhenSet() {
        String customPrivacy = "# Custom Privacy Policy\n\nThis is custom content.";
        String customTerms = "# Custom Terms\n\nThese are custom terms.";

        Jurisdiction j = new Jurisdiction("policy-test-2", 1L, "Custom Policy Jurisdiction", null);
        RemoteHost h = new RemoteHost("policyhost2");
        h.setJurisdiction(j);
        j.getRemoteHosts().add(h);
        j.setPrivacyPolicyContent(customPrivacy);
        j.setTermsOfUseContent(customTerms);
        j = jurisdictionRepository.save(j);
        jurisdictionBoundaryService.saveBoundary(j, DEFAULT_BOUNDS);

        HttpRequest<?> request = HttpRequest.GET("/config")
            .header("referer", "http://policyhost2");
        HttpResponse<JurisdictionDTO> response = client.toBlocking()
            .exchange(request, JurisdictionDTO.class);
        JurisdictionDTO infoResponse = response.getBody().get();

        // Should return custom content, not defaults
        assertEquals(customPrivacy, infoResponse.getPrivacyPolicyContent());
        assertEquals(customTerms, infoResponse.getTermsOfUseContent());
    }

    @Test
    public void deletionOfServiceShouldCascadeToServiceRequests() {

        // build service, attribute with values, service requests
        Jurisdiction jurisdiction = jurisdictionRepository.save(
                new Jurisdiction("fakecity.gov", 1L));
        jurisdictionBoundaryService.saveBoundary(jurisdiction, DEFAULT_BOUNDS);
        Jurisdiction savedJurisdiction = jurisdictionRepository.save(new Jurisdiction("faketown.gov", 1L));
        ServiceGroup infrastructureGroup = serviceGroupRepository.save(
                new ServiceGroup("TempInfrastructure", savedJurisdiction));
        ServiceDefinition sidewalkServiceDefinition = new ServiceDefinition("SidewalkServiceToBeDeleted");
        sidewalkServiceDefinition.setType(ServiceType.REALTIME);
        sidewalkServiceDefinition.setJurisdiction(savedJurisdiction);
        sidewalkServiceDefinition.setServiceGroup(infrastructureGroup);

        ServiceDefinition serviceDefinitionToBeDeleted = serviceRepository.save(sidewalkServiceDefinition);

        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(serviceDefinitionToBeDeleted);
        serviceDefinitionAttribute.setVariable(true);
        serviceDefinitionAttribute.setDatatype(AttributeDataType.MULTIVALUELIST);
        serviceDefinitionAttribute.setRequired(false);
        serviceDefinitionAttribute.setDescription(
                "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        serviceDefinitionAttribute.setAttributeOrder(1);
        serviceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");

        ServiceDefinitionAttribute savedSDA = serviceDefinitionAttributeRepository.save(
                serviceDefinitionAttribute);

        AttributeValue adaAccess = attributeValueRepository.save(new AttributeValue(savedSDA, "ADA Access"));
        attributeValueRepository.save(new AttributeValue(savedSDA, "Cracked"));
        attributeValueRepository.save(new AttributeValue(savedSDA, "Too narrow"));
        attributeValueRepository.save(new AttributeValue(savedSDA, "Heaved/Uneven Sidewalk"));
        attributeValueRepository.save(new AttributeValue(savedSDA, "Other"));

        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setJurisdiction(savedJurisdiction);
        serviceRequest.setService(serviceDefinitionToBeDeleted);
        setLocation(serviceRequest, IN_BOUNDS_COORDINATE);
        ServiceRequest savedServiceRequest = serviceRequestRepository.save(serviceRequest);

        // delete service
        serviceRepository.delete(serviceDefinitionToBeDeleted);

        // verify deletion of all entities
        assertFalse(serviceRepository.existsById(serviceDefinitionToBeDeleted.getId()));
        assertFalse(serviceDefinitionAttributeRepository.existsById(savedSDA.getId()));
        assertFalse(attributeValueRepository.existsById(adaAccess.getId()));
        assertFalse(serviceRequestRepository.existsById(savedServiceRequest.getId()));
    }

    private HttpResponse<?> createServiceRequest(Long serviceCode, String address, Map attributes, String jurisdictionId) {
        ServiceRequestPostRequest serviceRequestDTO = new ServiceRequestPostRequest(serviceCode);
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

    private HttpResponse<?> createServiceRequest(ServiceRequestPostRequest serviceRequestDTO, Map attributes, String jurisdictionId) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);
        payload.putAll(attributes);
        HttpRequest<?> request = HttpRequest.POST("/requests?jurisdiction_id="+jurisdictionId, payload)
            .header("Authorization", "Bearer token.text.here")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, Map.class);
    }

    void setLocation(ServiceRequest sr, Coordinate coordinate){
        sr.setLocation(libreGeometryFactory.createPoint(coordinate));
    }
}