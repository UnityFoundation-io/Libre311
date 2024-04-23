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

import app.dto.download.CsvHeaders;
import static app.util.JurisdictionBoundaryUtil.DEFAULT_BOUNDS;
import static app.util.JurisdictionBoundaryUtil.IN_BOUNDS_COORDINATE;
import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static io.micronaut.http.HttpStatus.OK;
import static io.micronaut.http.HttpStatus.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.dto.group.CreateUpdateGroupDTO;
import app.dto.group.GroupDTO;
import app.dto.service.CreateServiceDTO;
import app.dto.service.PatchServiceOrderPositionDTO;
import app.dto.service.ServiceDTO;
import app.dto.service.UpdateServiceDTO;
import app.dto.servicedefinition.*;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdictionuser.JurisdictionUser;
import app.model.jurisdictionuser.JurisdictionUserRepository;
import app.model.service.AttributeDataType;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.ServiceType;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicedefinition.AttributeValue;
import app.model.servicedefinition.AttributeValueRepository;
import app.model.servicedefinition.ServiceDefinitionAttribute;
import app.model.servicedefinition.ServiceDefinitionAttributeRepository;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestStatus;
import app.model.user.User;
import app.model.user.UserRepository;
import app.security.HasPermissionResponse;
import app.service.jurisdiction.JurisdictionBoundaryService;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import java.util.*;

@MicronautTest(transactional = false)
public class JurisdictionAdminControllerTest  {

    @Inject
    @Client("/api")
    HttpClient client;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    DbCleanup dbCleanup;

    @Inject
    UserRepository userRepository;

    @Inject
    JurisdictionUserRepository jurisdictionUserRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    ServiceDefinitionAttributeRepository serviceDefinitionAttributeRepository;

    @Inject
    AttributeValueRepository attributeValueRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    JurisdictionBoundaryService jurisdictionBoundaryService;

    private Service sidewalkService;
    private ServiceDefinitionAttribute savedSDA;
    private HashMap<String, Long> sidewalkAttrIdMap = new HashMap<>();

    @BeforeEach
    void setup() {
        mockAuthenticationFetcher.setAuthentication(null);
        setAuthHasPermissionSuccessResponse(false, null);

        setupMockData();
    }

    @AfterEach
    void teardown(){
        dbCleanup.cleanupAll();
    }

    public void setupMockData() {
        User admin = userRepository.save(new User("admin@fakecity.gov"));
        User user = userRepository.save(new User("user@fakecity.gov"));
        Jurisdiction jurisdiction = jurisdictionRepository.save(
            new Jurisdiction("fakecity.gov", 1L));
        jurisdictionBoundaryService.saveBoundary(jurisdiction, DEFAULT_BOUNDS);
        jurisdictionRepository.save(new Jurisdiction("faketown.gov", 1L));

        jurisdictionUserRepository.save(new JurisdictionUser(admin, jurisdiction, true));
        jurisdictionUserRepository.save(new JurisdictionUser(user, jurisdiction, false));

        // service
        ServiceGroup infrastructureGroup = serviceGroupRepository.save(
            new ServiceGroup("Infrastructure", jurisdiction));
        Service sidewalkService = new Service("Sidewalk");
        sidewalkService.setType(ServiceType.REALTIME);
        sidewalkService.setJurisdiction(jurisdiction);
        sidewalkService.setServiceGroup(infrastructureGroup);

        this.sidewalkService = serviceRepository.save(sidewalkService);

        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(sidewalkService);
        serviceDefinitionAttribute.setVariable(true);
        serviceDefinitionAttribute.setDatatype(AttributeDataType.MULTIVALUELIST);
        serviceDefinitionAttribute.setRequired(false);
        serviceDefinitionAttribute.setDescription(
            "Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
        serviceDefinitionAttribute.setAttributeOrder(1);
        serviceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");

        savedSDA = serviceDefinitionAttributeRepository.save(
            serviceDefinitionAttribute);

        sidewalkAttrIdMap.put("ADA Access",
                attributeValueRepository.save(new AttributeValue(savedSDA, "ADA Access")).getId());
        sidewalkAttrIdMap.put("Cracked",
                attributeValueRepository.save(new AttributeValue(savedSDA, "Cracked")).getId());
        sidewalkAttrIdMap.put("Too narrow",
                attributeValueRepository.save(new AttributeValue(savedSDA, "Too narrow")).getId());
        sidewalkAttrIdMap.put("Heaved/Uneven Sidewalk",
                attributeValueRepository.save(new AttributeValue(savedSDA, "Heaved/Uneven Sidewalk")).getId());
        sidewalkAttrIdMap.put("Other",
                attributeValueRepository.save(new AttributeValue(savedSDA, "Other")).getId());
    }

    private void setAuthHasPermissionSuccessResponse(boolean success, List<String> permissions) {
        if (success) {
            mockUnityAuthClient.setResponse(HttpResponse.ok(
                new HasPermissionResponse(true, "admin@fakecity.gov", null, permissions)));
        } else {
            mockUnityAuthClient.setResponse(HttpResponse.ok(
                new HasPermissionResponse(false, "user@fakecity.gov", "Unauthorized",
                    permissions)));
        }
    }

    void authLogin() {
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        setAuthHasPermissionSuccessResponse(true, null);
    }

    @Test
    void canPerformCrudActionsOnGroupIfAuthenticated() {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        // create
        response = createGroup("Sanitation", "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> optional = response.getBody(GroupDTO.class);
        assertTrue(optional.isPresent());
        GroupDTO groupDTO = optional.get();

        // list
        request = HttpRequest.GET("/jurisdiction-admin/groups?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO[].class);
        assertEquals(OK, response.getStatus());
        Optional<GroupDTO[]> listBody = response.getBody(GroupDTO[].class);
        assertTrue(listBody.isPresent());
        assertNotEquals(0, listBody.get().length);

        // update
        CreateUpdateGroupDTO updateGroupDTO = new CreateUpdateGroupDTO();
        updateGroupDTO.setName("Solid Waste Management");

        request = HttpRequest.PATCH(
                "/jurisdiction-admin/groups/" + groupDTO.getId() + "?jurisdiction_id=fakecity.gov",
                updateGroupDTO)
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO.class);
        assertEquals(OK, response.getStatus());

        // verify all
        Optional<GroupDTO> body = response.getBody(GroupDTO.class);
        assertTrue(body.isPresent());
        GroupDTO groupDTO1 = body.get();
        assertEquals("Solid Waste Management", groupDTO1.getName());

        // delete
        request = HttpRequest.DELETE(
                "/jurisdiction-admin/groups/" + groupDTO.getId() + "?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO.class);
        assertEquals(OK, response.getStatus());
    }

    @Test
    public void cannotCreateServiceForAnotherJurisdictionIfAuthenticated() {

        // Checks whether the user has the permission to create a service in their own jurisdiction, but does
        // not have a user-jurisdiction record locally.
        mockUnityAuthClient.setResponse(HttpResponse.ok(
            new HasPermissionResponse(true, "person2@test.com", null,
                List.of("LIBRE311_ADMIN_VIEW_SUBTENANT"))));

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById(
            "fakecity.gov");
        ServiceGroup bikeln010Group = serviceGroupRepository.save(
            new ServiceGroup("BIKELN010 Group", optionalJurisdiction.get()));

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class, () -> {
                createService("Bike Lane Obstruction", "faketown.gov",
                    bikeln010Group.getId());
            });
        assertEquals(UNAUTHORIZED, exception.getStatus());
    }

    // create service
    @Test
    public void canCreateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById(
            "fakecity.gov");
        ServiceGroup bikeln007Group = serviceGroupRepository.save(
            new ServiceGroup("BIKELN007 Group", optionalJurisdiction.get()));

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class, () -> {
                createService("Bike Lane Obstruction", "fakecity.gov",
                    bikeln007Group.getId());
            });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // success, bare minimum
        response = createService("Bike Lane Obstruction", "fakecity.gov",
            bikeln007Group.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        assertEquals(-1, optional.get().getOrderPosition());

        // success, all provided
        response = createService("Bike Lane Obstruction", "fakecity.gov", bikeln007Group.getId(), 2);
        assertEquals(HttpStatus.OK, response.getStatus());
        optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO serviceDTO = optional.get();
        assertNotNull(serviceDTO.getJurisdictionId());
        assertEquals("fakecity.gov", serviceDTO.getJurisdictionId());
        assertEquals(2, optional.get().getOrderPosition());

        // fail, jurisdiction not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("Road Issues", null, 1L);
        });
        assertEquals(NOT_FOUND, exception.getStatus());

        // fail, name not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService(null, "fakecity.gov", 1L);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());

        // fail, group not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("Road Issues", "fakecity.gov", null);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    // update service
    @Test
    public void canUpdateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        response = createGroup("Group - Bus Stop 1", "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> groupOptional = response.getBody(GroupDTO.class);
        assertTrue(groupOptional.isPresent());
        GroupDTO groupDTO = groupOptional.get();

        response = createService("Bus Stop Issues", "fakecity.gov",
            groupDTO.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO serviceDTO = optional.get();

        response = addServiceDefinitionAttribute(serviceDTO.getId(), "fakecity.gov",
            new CreateServiceDefinitionAttributeDTO(
                true,
                AttributeDataType.STRING,
                false,
                "Bus Stop Near",
                1,
                "(Optional) If the issue is near anything, please describe here."
            ));
        assertEquals(HttpStatus.OK, response.getStatus());

        CreateServiceDefinitionAttributeDTO sdaIssueSelect = new CreateServiceDefinitionAttributeDTO(
            true,
            AttributeDataType.MULTIVALUELIST,
            true,
            "Bus Stop Issues",
            2,
            "(Optional) If the issue is near anything, please describe here."
        );

        // Cannot create MULTIVALUELIST attribute with empty values
        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            addServiceDefinitionAttribute(serviceDTO.getId(), "fakecity.gov", sdaIssueSelect);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());

        sdaIssueSelect.setValues(List.of(
            new AttributeValueDTO("UNSAFE", "Unsafe location"),
            new AttributeValueDTO("NO_SDLWLK", "No Sidewalk present"),
            new AttributeValueDTO("MISSING_SIGN", "Sign is missing")
        ));

        response = addServiceDefinitionAttribute(serviceDTO.getId(), "fakecity.gov",
            sdaIssueSelect);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDefinitionDTO> optionalServiceDefinition = response.getBody(
            ServiceDefinitionDTO.class);
        assertTrue(optionalServiceDefinition.isPresent());
        ServiceDefinitionDTO savedServiceDefinitionDTO = optionalServiceDefinition.get();
        assertNotNull(savedServiceDefinitionDTO.getAttributes());
        assertFalse(savedServiceDefinitionDTO.getAttributes().isEmpty());
        assertEquals(2, savedServiceDefinitionDTO.getAttributes().size());

        assertEquals(groupDTO.getId(), serviceDTO.getGroupId());

        response = createGroup("Group - Bus Stop 2", "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> secondGroupOptional = response.getBody(GroupDTO.class);
        assertTrue(secondGroupOptional.isPresent());
        GroupDTO secondGroupDTO = secondGroupOptional.get();

        UpdateServiceDTO updateServiceDTO = new UpdateServiceDTO();

        // update fail
        updateServiceDTO.setServiceName("   ");
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            HttpRequest failUpdateRequest = HttpRequest.PATCH(
                            "/jurisdiction-admin/services/" + serviceDTO.getId() + "?jurisdiction_id=fakecity.gov",
                            updateServiceDTO)
                    .header("Authorization", "Bearer token.text.here");
            client.toBlocking().exchange(failUpdateRequest, ServiceDTO.class);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());

        // update Service
        updateServiceDTO.setServiceName("Inner City Bust Stops");
        updateServiceDTO.setDescription("Issues pertaining to inner city bus stops.");
        updateServiceDTO.setOrderPosition(3);
        updateServiceDTO.setGroupId(secondGroupDTO.getId());

        request = HttpRequest.PATCH(
                "/jurisdiction-admin/services/" + serviceDTO.getId() + "?jurisdiction_id=fakecity.gov",
                updateServiceDTO)
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO.class);
        assertEquals(OK, response.getStatus());

        Optional<ServiceDTO> body = response.getBody(ServiceDTO.class);
        assertTrue(body.isPresent());
        ServiceDTO serviceDTO1 = body.get();
        assertEquals("Inner City Bust Stops", serviceDTO1.getServiceName());
        assertEquals("Issues pertaining to inner city bus stops.", serviceDTO1.getDescription());
        assertEquals(secondGroupDTO.getId(), serviceDTO1.getGroupId());
        assertEquals(3, serviceDTO1.getOrderPosition());

        // Remove ISSUE_NEAR attribute
        Optional<ServiceDefinitionAttributeDTO> issueNearOptional = savedServiceDefinitionDTO.getAttributes()
            .stream()
            .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getDescription()
                .equals("Bus Stop Near"))
            .findFirst();
        assertTrue(issueNearOptional.isPresent());
        ServiceDefinitionAttributeDTO issueNearAttribute = issueNearOptional.get();

        request = HttpRequest.DELETE(
                "/jurisdiction-admin/services/" + serviceDTO.getId() + "/attributes/"
                    + issueNearAttribute.getId() + "?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO.class);
        assertEquals(OK, response.getStatus());

        // Update ISSUE_SELECT attribute
        Optional<ServiceDefinitionAttributeDTO> issueSelectOptional = savedServiceDefinitionDTO.getAttributes()
            .stream()
            .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getDescription()
                .equals("Bus Stop Issues"))
            .findFirst();
        assertTrue(issueSelectOptional.isPresent());
        ServiceDefinitionAttributeDTO issueSelectAttribute = issueSelectOptional.get();

        UpdateServiceDefinitionAttributeDTO patchSDA = new UpdateServiceDefinitionAttributeDTO();
        patchSDA.setAttributeOrder(1);
        request = HttpRequest.PATCH(
                "/jurisdiction-admin/services/" + serviceDTO.getId() + "/attributes/"
                    + issueSelectAttribute.getId() + "?jurisdiction_id=fakecity.gov", patchSDA)
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDefinitionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<ServiceDefinitionDTO> optionalPatchServiceDefinition = response.getBody(
            ServiceDefinitionDTO.class);
        assertTrue(optionalPatchServiceDefinition.isPresent());
        ServiceDefinitionDTO patchedServiceDefinitionDTO = optionalPatchServiceDefinition.get();
        assertNotNull(patchedServiceDefinitionDTO.getAttributes());
        assertFalse(patchedServiceDefinitionDTO.getAttributes().isEmpty());
        assertEquals(1, patchedServiceDefinitionDTO.getAttributes().size());

        // get service definition
        response = client.toBlocking()
            .exchange("/services/" + serviceDTO1.getId() + "?jurisdiction_id=fakecity.gov",
                String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinitionResponse = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinitionResponse));
        ServiceDefinitionDTO serviceDefinitionDTOObject = (new ObjectMapper()).readValue(
            serviceDefinitionResponse, ServiceDefinitionDTO.class);
        assertNotNull(serviceDefinitionDTOObject.getServiceCode());
        assertEquals(serviceDTO1.getId(), serviceDefinitionDTOObject.getServiceCode());
        assertNotNull(serviceDefinitionDTOObject.getAttributes());
        assertFalse(serviceDefinitionDTOObject.getAttributes().isEmpty());
        assertEquals(1, serviceDefinitionDTOObject.getAttributes().size());
        assertTrue(serviceDefinitionDTOObject.getAttributes().stream()
            .anyMatch(serviceDefinitionAttribute ->
                serviceDefinitionAttribute.getDescription().equals("Bus Stop Issues") &&
                    serviceDefinitionAttribute.getValues() != null &&
                    !serviceDefinitionAttribute.getValues().isEmpty() &&
                    serviceDefinitionAttribute.getAttributeOrder() == 1)
        );
    }

    @Test
    void canUpdateServicesOrder() {
        HttpResponse<?> response;

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById("fakecity.gov");
        ServiceGroup myInfraGroup = serviceGroupRepository.save(new ServiceGroup("MyInfraGroup", optionalJurisdiction.get()));

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId());
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        response = createService("Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO bikeLaneService = optional.get();
        assertEquals(-1, bikeLaneService.getOrderPosition());

        response = createService("Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId(), 2);
        assertEquals(HttpStatus.OK, response.getStatus());
        optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO busStopService = optional.get();
        assertEquals(2, busStopService.getOrderPosition());

        // update order
        List<PatchServiceOrderPositionDTO> payload = List.of(
                new PatchServiceOrderPositionDTO(bikeLaneService.getId(), 0),
                new PatchServiceOrderPositionDTO(busStopService.getId(), 1)
        );

        HttpRequest<?> request = HttpRequest.PATCH(
                "/jurisdiction-admin/groups/"+myInfraGroup.getId()+"/services-order?jurisdiction_id=fakecity.gov",
                        payload).header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO[]> serviceDTOSOptional = response.getBody(ServiceDTO[].class);
        assertTrue(serviceDTOSOptional.isPresent());
        ServiceDTO[] serviceDTOS = serviceDTOSOptional.get();
        assertEquals(2, serviceDTOS.length);
        assertTrue(Arrays.stream(serviceDTOS)
                .allMatch(serviceDTO -> (Objects.equals(serviceDTO.getId(), bikeLaneService.getId()) && serviceDTO.getOrderPosition() == 0) ||
                        (Objects.equals(serviceDTO.getId(), busStopService.getId()) && serviceDTO.getOrderPosition() == 1)));
    }

    @Test
    void canUpdateAttributesOrder() {
        HttpResponse<?> response;

        ServiceDefinitionAttribute serviceDefinitionAttribute = new ServiceDefinitionAttribute();
        serviceDefinitionAttribute.setService(sidewalkService);
        serviceDefinitionAttribute.setVariable(true);
        serviceDefinitionAttribute.setDatatype(AttributeDataType.STRING);
        serviceDefinitionAttribute.setRequired(true);
        serviceDefinitionAttribute.setDescription("Sidewalk Service");
        serviceDefinitionAttribute.setAttributeOrder(3);
        serviceDefinitionAttribute.setDatatypeDescription("Sidewalk Service");
        ServiceDefinitionAttribute stringAttribute = serviceDefinitionAttributeRepository.save(serviceDefinitionAttribute);

        authLogin();

        // reverse the order
        List<PatchAttributeOrderDTO> payload = List.of(
                new PatchAttributeOrderDTO(stringAttribute.getId(), 0),
                new PatchAttributeOrderDTO(savedSDA.getId(), 1)
        );

        HttpRequest<?> request = HttpRequest.PATCH(
                "/jurisdiction-admin/services/"+sidewalkService.getId()+"/attributes-order?jurisdiction_id=fakecity.gov",
                payload).header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDefinitionDTO.class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDefinitionDTO> serviceDefinitionDTOOptional = response.getBody(ServiceDefinitionDTO.class);
        assertTrue(serviceDefinitionDTOOptional.isPresent());
        ServiceDefinitionDTO serviceDefinitionDTO = serviceDefinitionDTOOptional.get();
        assertTrue(serviceDefinitionDTO.getAttributes().stream().allMatch(
                attributeDTO -> (Objects.equals(attributeDTO.getId(), savedSDA.getId()) && attributeDTO.getAttributeOrder() == 1) ||
                        (Objects.equals(attributeDTO.getId(), stringAttribute.getId()) && attributeDTO.getAttributeOrder() == 0)));
    }

    @Test
    void canAssociateAGroupToAService() {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        // create
        response = createGroup("Animal Control", "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> groupOptional = response.getBody(GroupDTO.class);
        assertTrue(groupOptional.isPresent());
        GroupDTO groupDTO = groupOptional.get();

        response = createService("Animal in Distress", "fakecity.gov",
            groupDTO.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> serviceOptional = response.getBody(ServiceDTO.class);
        assertTrue(serviceOptional.isPresent());

        // delete
        request = HttpRequest.DELETE(
                "/jurisdiction-admin/groups/" + groupDTO.getId() + "?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO[].class);
        assertEquals(OK, response.getStatus());
    }

    // update
    @Test
    public void canUpdateServiceRequestIfAuthenticated() {
        HttpResponse<?> response;

        response = createSidewalkServiceRequest("12345 Fairway",
            Map.of("attribute["+savedSDA.getId()+"]", sidewalkAttrIdMap.get("Cracked")), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<PostResponseServiceRequestDTO[]> optional = response.getBody(
            PostResponseServiceRequestDTO[].class);
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
            .PATCH("/jurisdiction-admin/requests/" + postResponseServiceRequestDTO.getId()
                + "?jurisdiction_id=fakecity.gov", payload)
            .header("Authorization", "Bearer token.text.here");

        HttpRequest<?> finalRequest = request;
        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class, () -> {
                client.toBlocking().exchange(finalRequest, SensitiveServiceRequestDTO.class);
            });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // update
        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO.class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<SensitiveServiceRequestDTO> bodyOptional = response.getBody(
            SensitiveServiceRequestDTO.class);
        assertTrue(bodyOptional.isPresent());
        SensitiveServiceRequestDTO updatedServiceRequestDTO = bodyOptional.get();
        assertEquals("acme@example.com", updatedServiceRequestDTO.getAgencyEmail());
        assertEquals("To be fulfilled by Acme Concrete Co.",
            updatedServiceRequestDTO.getServiceNotice());
        assertEquals("Acme Concrete", updatedServiceRequestDTO.getAgencyResponsible());
        assertEquals("Will investigate and remediate within 2 weeks",
            updatedServiceRequestDTO.getStatusNotes());
        assertEquals(ServiceRequestPriority.HIGH, updatedServiceRequestDTO.getPriority());
        assertEquals(ServiceRequestStatus.IN_PROGRESS, updatedServiceRequestDTO.getStatus());

        // update dates
        request = HttpRequest
            .PATCH("/jurisdiction-admin/requests/" + postResponseServiceRequestDTO.getId()
                    + "?jurisdiction_id=fakecity.gov",
                Map.of(
                    "jurisdiction_id", "fakecity.gov",
                    "closed_datetime", "2023-01-25T13:15:30Z",
                    "expected_datetime", "2023-01-15T13:15:30Z"
                ))
            .header("Authorization", "Bearer token.text.here");

        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO.class);
        assertEquals(HttpStatus.OK, response.status());
        bodyOptional = response.getBody(SensitiveServiceRequestDTO.class);
        assertTrue(bodyOptional.isPresent());
        updatedServiceRequestDTO = bodyOptional.get();
        assertNotNull(updatedServiceRequestDTO.getClosedDate());
        assertNotNull(updatedServiceRequestDTO.getExpectedDate());
    }

    @Test
    public void canDownloadCSVFile() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createSidewalkServiceRequest("12345 Nearway",
            Map.of("attribute["+savedSDA.getId()+"]", sidewalkAttrIdMap.get("Cracked")), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("6789 Faraway",
            Map.of("attribute["+savedSDA.getId()+"]", sidewalkAttrIdMap.get("Too narrow")), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        HttpRequest<?> request = HttpRequest.GET(
                "/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");

        HttpClientResponseException exception = assertThrowsExactly(
            HttpClientResponseException.class, () -> {
                client.toBlocking().exchange(request, byte[].class);
            });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.get());
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).setHeader(CsvHeaders.class).build();
        CSVParser parser = new CSVParser(inputStreamReader, csvFormat);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 1);
    }

    @Test
    public void downloadCSVRequestsFileCanBeSorted() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createSidewalkServiceRequest("a st.",
                Map.of("attribute["+savedSDA.getId()+"]", sidewalkAttrIdMap.get("Cracked")), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("x st.",
                Map.of("attribute["+savedSDA.getId()+"]", sidewalkAttrIdMap.get("Too narrow")), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        HttpRequest<?> request = HttpRequest.GET("/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov&sort=addressString,asc")
                .header("Authorization", "Bearer token.text.here");

        authLogin();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.get());
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).setHeader(CsvHeaders.class).build();
        CSVParser parser = new CSVParser(inputStreamReader, csvFormat);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 1);

        CSVRecord firstRecord = records.get(0);
        assertEquals("a st.",  firstRecord.get("ADDRESS"));

        // address descending
        request = HttpRequest.GET("/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov&sort=addressString,desc")
                .header("Authorization", "Bearer token.text.here");

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        body = response.getBody(byte[].class);
        assertTrue(body.isPresent());

        byteArrayInputStream = new ByteArrayInputStream(body.get());
        inputStreamReader = new InputStreamReader(byteArrayInputStream);
        parser = new CSVParser(inputStreamReader, csvFormat);
        records = parser.getRecords();
        assertTrue(records.size() > 1);

        firstRecord = records.get(0);
        assertEquals("x st.",  firstRecord.get("ADDRESS"));
    }

    @Test
    public void theCSVFileShouldNotContainCellsBeginningWithUnsafeCharacters() throws IOException {
        HttpResponse<?> response;

        Long sidewalkId = savedSDA.getId();
        Long heavedUnevenId = sidewalkAttrIdMap.get("Heaved/Uneven Sidewalk");

        // create service requests
        response = createSidewalkServiceRequest("=1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("@1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("+1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("-1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("\t1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createSidewalkServiceRequest("\r1+3",
            Map.of("attribute["+sidewalkId+"]", heavedUnevenId), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        HttpRequest<?> request = HttpRequest.GET(
                "/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov")
            .header("Authorization", "Bearer token.text.here");

        authLogin();

        response = client.toBlocking().exchange(request, byte[].class);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<byte[]> body = response.getBody(byte[].class);
        assertTrue(body.isPresent());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.get());
        InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream);
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setSkipHeaderRecord(true).setHeader(CsvHeaders.class).build();
        CSVParser parser = new CSVParser(inputStreamReader, csvFormat);
        List<CSVRecord> records = parser.getRecords();
        assertTrue(records.size() > 1);

        CSVRecord firstRecord = records.get(0);
        String address = firstRecord.get("ADDRESS");
        assertTrue(address.startsWith("'"));
    }

    private HttpResponse<?> createGroup(String name, String jurisdictionId) {
        CreateUpdateGroupDTO groupDTO = new CreateUpdateGroupDTO();
        groupDTO.setName(name);
        HttpRequest<?> request = HttpRequest.POST(
                "/jurisdiction-admin/groups?jurisdiction_id=" + jurisdictionId, groupDTO)
            .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, GroupDTO[].class);
    }

    private HttpResponse<?> createService(String name, String jurisdictionId, Long groupId) {
        return createService(name, jurisdictionId, groupId, null);
    }

    private HttpResponse<?> createService(String name, String jurisdictionId, Long groupId, Integer orderPosition) {
        CreateServiceDTO serviceDTO = new CreateServiceDTO();
        serviceDTO.setServiceName(name);
        serviceDTO.setGroupId(groupId);
        serviceDTO.setOrderPosition(orderPosition);

        HttpRequest<?> request = HttpRequest.POST("/jurisdiction-admin/services?jurisdiction_id="+jurisdictionId, serviceDTO)
                .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, ServiceDTO.class);
    }

    private HttpResponse<?> addServiceDefinitionAttribute(Long serviceId, String jurisdictionId,
        CreateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO) {
        HttpRequest<?> request = HttpRequest.POST(
                "/jurisdiction-admin/services/" + serviceId + "/attributes?jurisdiction_id="
                    + jurisdictionId, serviceDefinitionAttributeDTO)
            .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, ServiceDefinitionDTO.class);
    }

    private HttpResponse<?> createSidewalkServiceRequest(String address, Map attributes,
                                                         String jurisdictionId) {

        PostRequestServiceRequestDTO serviceRequestDTO = new PostRequestServiceRequestDTO(sidewalkService.getId());
        serviceRequestDTO.setgRecaptchaResponse("abc");

        serviceRequestDTO.setLongitude(String.valueOf(IN_BOUNDS_COORDINATE.getX()));
        serviceRequestDTO.setLatitude(String.valueOf(IN_BOUNDS_COORDINATE.getY()));
        if (address != null) {
            serviceRequestDTO.setAddressString(address);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Map payload = objectMapper.convertValue(serviceRequestDTO, Map.class);
        payload.putAll(attributes);
        HttpRequest<?> request = HttpRequest.POST("/requests?jurisdiction_id=" + jurisdictionId,
                payload)
            .header("Authorization", "Bearer token.text.here")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED);
        return client.toBlocking().exchange(request, Map.class);
    }
}
