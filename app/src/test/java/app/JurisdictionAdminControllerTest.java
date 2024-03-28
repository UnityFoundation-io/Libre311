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
import app.model.servicedefinition.*;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestStatus;
import app.model.user.User;
import app.model.user.UserRepository;
import app.security.HasPermissionResponse;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
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
import java.util.*;

import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static io.micronaut.http.HttpStatus.*;
import static io.micronaut.http.HttpStatus.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class JurisdictionAdminControllerTest {

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

    @BeforeEach
    void setup() {
        dbCleanup.cleanupServiceRequests();
        mockAuthenticationFetcher.setAuthentication(null);
        setAuthHasPermissionSuccessResponse(false, null);

        setupMockData();
    }

    public void setupMockData() {
        if (userRepository.findByEmail("admin@fakecity.gov").isEmpty() &&
                userRepository.findByEmail("user@fakecity.gov").isEmpty() &&
                jurisdictionRepository.findById("fakecity.gov").isEmpty()) {
            User admin = userRepository.save(new User("admin@fakecity.gov"));
            User user = userRepository.save(new User("user@fakecity.gov"));
            Jurisdiction jurisdiction = jurisdictionRepository.save(new Jurisdiction("fakecity.gov", 1L));
            jurisdictionRepository.save(new Jurisdiction("faketown.gov", 1L));

            jurisdictionUserRepository.save(new JurisdictionUser(admin, jurisdiction, true));
            jurisdictionUserRepository.save(new JurisdictionUser(user, jurisdiction, false));

            // service
            ServiceGroup infrastructureGroup = serviceGroupRepository.save(new ServiceGroup("Infrastructure", jurisdiction));
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
            serviceDefinitionAttribute.setDescription("Please select one or more items that best describe the issue. If Other, please elaborate in the Description field below.");
            serviceDefinitionAttribute.setAttributeOrder(1);
            serviceDefinitionAttribute.setDatatypeDescription("Please select one or more items.");

            ServiceDefinitionAttribute savedSDA = serviceDefinitionAttributeRepository.save(serviceDefinitionAttribute);

            attributeValueRepository.save(new AttributeValue(savedSDA, "ADA Access"));
            attributeValueRepository.save(new AttributeValue(savedSDA, "Cracked"));
            attributeValueRepository.save(new AttributeValue(savedSDA, "Too narrow"));
            attributeValueRepository.save(new AttributeValue(savedSDA, "Heaved/Uneven Sidewalk"));
            attributeValueRepository.save(new AttributeValue(savedSDA, "Other"));
        }
    }

    private void setAuthHasPermissionSuccessResponse(boolean success, List<String> permissions) {
        if (success) {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(true, "admin@fakecity.gov", null, permissions)));
        } else {
            mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(false, "user@fakecity.gov", "Unauthorized", permissions)));
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

        request = HttpRequest.PATCH("/jurisdiction-admin/groups/"+groupDTO.getId()+"?jurisdiction_id=fakecity.gov", updateGroupDTO)
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO.class);
        assertEquals(OK, response.getStatus());

        // verify all
        Optional<GroupDTO> body = response.getBody(GroupDTO.class);
        assertTrue(body.isPresent());
        GroupDTO groupDTO1 = body.get();
        assertEquals("Solid Waste Management", groupDTO1.getName());

        // delete
        request = HttpRequest.DELETE("/jurisdiction-admin/groups/"+groupDTO.getId()+"?jurisdiction_id=fakecity.gov")
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO.class);
        assertEquals(OK, response.getStatus());
    }

    @Test
    public void cannotCreateServiceForAnotherJurisdictionIfAuthenticated() {

        // Checks whether the user has the permission to create a service in their own jurisdiction, but does
        // not have a user-jurisdiction record locally.
        mockUnityAuthClient.setResponse(HttpResponse.ok(new HasPermissionResponse(true, "person2@test.com", null, List.of("LIBRE311_ADMIN_VIEW_SUBTENANT"))));

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById("fakecity.gov");
        ServiceGroup bikeln010Group = serviceGroupRepository.save(new ServiceGroup("BIKELN010 Group", optionalJurisdiction.get()));

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("BIKELN010", "Bike Lane Obstruction", "faketown.gov", bikeln010Group.getId());
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());
    }

    // create service
    @Test
    public void canCreateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;

        Optional<Jurisdiction> optionalJurisdiction = jurisdictionRepository.findById("fakecity.gov");
        ServiceGroup bikeln007Group = serviceGroupRepository.save(new ServiceGroup("BIKELN007 Group", optionalJurisdiction.get()));

        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("BIKELN007", "Bike Lane Obstruction", "fakecity.gov", bikeln007Group.getId());
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // success, bare minimum
        response = createService("BIKELN007", "Bike Lane Obstruction", "fakecity.gov", bikeln007Group.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        assertEquals(-1, optional.get().getOrderPosition());

        // success, all provided
        response = createService("BUS_STOP", "Bike Lane Obstruction", "fakecity.gov", bikeln007Group.getId(), 2);
        assertEquals(HttpStatus.OK, response.getStatus());
        optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO serviceDTO = optional.get();
        assertNotNull(serviceDTO.getJurisdictionId());
        assertEquals("fakecity.gov", serviceDTO.getJurisdictionId());
        assertEquals(2, optional.get().getOrderPosition());

        // fail, jurisdiction not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("ROAD", "Road Issues", null, 1L);
        });
        assertEquals(NOT_FOUND, exception.getStatus());

        // fail, name not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("ROAD", null, "fakecity.gov", 1L);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());

        // fail, group not provided
        exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            createService("ROAD", "Road Issues", "fakecity.gov", null);
        });
        assertEquals(BAD_REQUEST, exception.getStatus());
    }

    // update service
    @Test
    public void canUpdateServiceIfAuthenticated() throws JsonProcessingException {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        response = createGroup("Group - Bus Stop 1","fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> groupOptional = response.getBody(GroupDTO.class);
        assertTrue(groupOptional.isPresent());
        GroupDTO groupDTO = groupOptional.get();

        response = createService("BUS_STOP_UPDATE", "Bus Stop Issues","fakecity.gov", groupDTO.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO serviceDTO = optional.get();

        response = addServiceDefinitionAttribute(serviceDTO.getId(), "fakecity.gov", new CreateServiceDefinitionAttributeDTO(
                "ISSUE_NEAR",
                true,
                AttributeDataType.STRING,
                false,
                "Bus Stop Near",
                1,
                "(Optional) If the issue is near anything, please describe here."
        ));
        assertEquals(HttpStatus.OK, response.getStatus());


        CreateServiceDefinitionAttributeDTO sdaIssueSelect = new CreateServiceDefinitionAttributeDTO(
                "ISSUE_SELECT",
                true,
                AttributeDataType.MULTIVALUELIST,
                true,
                "Bus Stop Issues",
                2,
                "(Optional) If the issue is near anything, please describe here."
        );
        sdaIssueSelect.setValues(List.of(
                new AttributeValueDTO("UNSAFE", "Unsafe location"),
                new AttributeValueDTO("NO_SDLWLK", "No Sidewalk present"),
                new AttributeValueDTO("MISSING_SIGN", "Sign is missing")
        ));

        response = addServiceDefinitionAttribute(serviceDTO.getId(), "fakecity.gov", sdaIssueSelect);
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDefinitionDTO> optionalServiceDefinition = response.getBody(ServiceDefinitionDTO.class);
        assertTrue(optionalServiceDefinition.isPresent());
        ServiceDefinitionDTO savedServiceDefinitionDTO = optionalServiceDefinition.get();
        assertNotNull(savedServiceDefinitionDTO.getAttributes());
        assertFalse(savedServiceDefinitionDTO.getAttributes().isEmpty());
        assertEquals(2, savedServiceDefinitionDTO.getAttributes().size());

        assertEquals(groupDTO.getId(), serviceDTO.getGroupId());

        // update Service
        UpdateServiceDTO updateServiceDTO = new UpdateServiceDTO();
        updateServiceDTO.setServiceCode("INNER_CITY_BUS_STOPS");
        updateServiceDTO.setServiceName("Inner City Bust Stops");
        updateServiceDTO.setDescription("Issues pertaining to inner city bus stops.");
        updateServiceDTO.setOrderPosition(3);

        response = createGroup("Group - Bus Stop 2","fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> secondGroupOptional = response.getBody(GroupDTO.class);
        assertTrue(secondGroupOptional.isPresent());
        GroupDTO secondGroupDTO = secondGroupOptional.get();
        updateServiceDTO.setGroupId(secondGroupDTO.getId());

        request = HttpRequest.PATCH("/jurisdiction-admin/services/"+serviceDTO.getId()+"?jurisdiction_id=fakecity.gov", updateServiceDTO)
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO.class);
        assertEquals(OK, response.getStatus());

        Optional<ServiceDTO> body = response.getBody(ServiceDTO.class);
        assertTrue(body.isPresent());
        ServiceDTO serviceDTO1 = body.get();
        assertEquals("INNER_CITY_BUS_STOPS", serviceDTO1.getServiceCode());
        assertEquals("Inner City Bust Stops", serviceDTO1.getServiceName());
        assertEquals("Issues pertaining to inner city bus stops.", serviceDTO1.getDescription());
        assertEquals(secondGroupDTO.getId(), serviceDTO1.getGroupId());
        assertEquals(3, serviceDTO1.getOrderPosition());

        // Remove ISSUE_NEAR attribute
        Optional<ServiceDefinitionAttributeDTO> issueNearOptional = savedServiceDefinitionDTO.getAttributes().stream()
                .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getCode().equals("ISSUE_NEAR"))
                .findFirst();
        assertTrue(issueNearOptional.isPresent());
        ServiceDefinitionAttributeDTO issueNearAttribute = issueNearOptional.get();

        request = HttpRequest.DELETE("/jurisdiction-admin/services/"+serviceDTO.getId()+"/attributes/"+issueNearAttribute.getId()+"?jurisdiction_id=fakecity.gov")
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDTO.class);
        assertEquals(OK, response.getStatus());

        // Update ISSUE_SELECT attribute
        Optional<ServiceDefinitionAttributeDTO> issueSelectOptional = savedServiceDefinitionDTO.getAttributes().stream()
                .filter(serviceDefinitionAttribute -> serviceDefinitionAttribute.getCode().equals("ISSUE_SELECT"))
                .findFirst();
        assertTrue(issueSelectOptional.isPresent());
        ServiceDefinitionAttributeDTO issueSelectAttribute = issueSelectOptional.get();

        UpdateServiceDefinitionAttributeDTO patchSDA = new UpdateServiceDefinitionAttributeDTO();
        patchSDA.setAttributeOrder(1);
        request = HttpRequest.PATCH("/jurisdiction-admin/services/"+serviceDTO.getId()+"/attributes/"+issueSelectAttribute.getId()+"?jurisdiction_id=fakecity.gov", patchSDA)
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, ServiceDefinitionDTO.class);
        assertEquals(OK, response.getStatus());
        Optional<ServiceDefinitionDTO> optionalPatchServiceDefinition = response.getBody(ServiceDefinitionDTO.class);
        assertTrue(optionalPatchServiceDefinition.isPresent());
        ServiceDefinitionDTO patchedServiceDefinitionDTO = optionalPatchServiceDefinition.get();
        assertNotNull(patchedServiceDefinitionDTO.getAttributes());
        assertFalse(patchedServiceDefinitionDTO.getAttributes().isEmpty());
        assertEquals(1, patchedServiceDefinitionDTO.getAttributes().size());

        // get service definition
        response = client.toBlocking().exchange("/services/"+serviceDTO1.getServiceCode()+"?jurisdiction_id=fakecity.gov", String.class);
        assertEquals(HttpStatus.OK, response.status());
        Optional<String> serviceDefinitionOptional = response.getBody(String.class);
        assertTrue(serviceDefinitionOptional.isPresent());
        String serviceDefinitionResponse = serviceDefinitionOptional.get();
        assertTrue(StringUtils.hasText(serviceDefinitionResponse));
        ServiceDefinitionDTO serviceDefinitionDTOObject = (new ObjectMapper()).readValue(serviceDefinitionResponse, ServiceDefinitionDTO.class);
        assertNotNull(serviceDefinitionDTOObject.getServiceCode());
        assertEquals("INNER_CITY_BUS_STOPS", serviceDefinitionDTOObject.getServiceCode());
        assertNotNull(serviceDefinitionDTOObject.getAttributes());
        assertFalse(serviceDefinitionDTOObject.getAttributes().isEmpty());
        assertEquals(1, serviceDefinitionDTOObject.getAttributes().size());
        assertTrue(serviceDefinitionDTOObject.getAttributes().stream()
                .anyMatch(serviceDefinitionAttribute ->
                        serviceDefinitionAttribute.getCode().equals("ISSUE_SELECT") &&
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
            createService("InfraBIKELN007", "Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId());
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        response = createService("InfraBIKELN007", "Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> optional = response.getBody(ServiceDTO.class);
        assertTrue(optional.isPresent());
        ServiceDTO bikeLaneService = optional.get();
        assertEquals(-1, bikeLaneService.getOrderPosition());

        response = createService("InfraBUS_STOP", "Bike Lane Obstruction", "fakecity.gov", myInfraGroup.getId(), 2);
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
    void canAssociateAGroupToAService() {
        HttpResponse<?> response;
        HttpRequest<?> request;

        authLogin();

        // create
        response = createGroup("Animal Control","fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<GroupDTO> groupOptional = response.getBody(GroupDTO.class);
        assertTrue(groupOptional.isPresent());
        GroupDTO groupDTO = groupOptional.get();

        response = createService("DISTRESSED_ANIMAL", "Animal in Distress", "fakecity.gov", groupDTO.getId());
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<ServiceDTO> serviceOptional = response.getBody(ServiceDTO.class);
        assertTrue(serviceOptional.isPresent());

        // delete
        request = HttpRequest.DELETE("/jurisdiction-admin/groups/"+groupDTO.getId()+"?jurisdiction_id=fakecity.gov")
                .header("Authorization", "Bearer token.text.here");
        response = client.toBlocking().exchange(request, GroupDTO[].class);
        assertEquals(OK, response.getStatus());
    }

    // update
    @Test
    public void canUpdateServiceRequestIfAuthenticated() {
        HttpResponse<?> response;

        response = createServiceRequest("001", "12345 Fairway",
                Map.of("attribute[SDWLK]", "CRACKED"), "fakecity.gov");
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
                .PATCH("/jurisdiction-admin/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=fakecity.gov", payload)
                .header("Authorization", "Bearer token.text.here");

        HttpRequest<?> finalRequest = request;
        HttpClientResponseException exception = assertThrowsExactly(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(finalRequest, SensitiveServiceRequestDTO.class);
        });
        assertEquals(UNAUTHORIZED, exception.getStatus());

        authLogin();

        // update
        response = client.toBlocking().exchange(request, SensitiveServiceRequestDTO.class);
        assertEquals(HttpStatus.OK, response.status());

        Optional<SensitiveServiceRequestDTO> bodyOptional = response.getBody(SensitiveServiceRequestDTO.class);
        assertTrue(bodyOptional.isPresent());
        SensitiveServiceRequestDTO updatedServiceRequestDTO =bodyOptional.get();
        assertEquals("acme@example.com", updatedServiceRequestDTO.getAgencyEmail());
        assertEquals("To be fulfilled by Acme Concrete Co.", updatedServiceRequestDTO.getServiceNotice());
        assertEquals("Acme Concrete", updatedServiceRequestDTO.getAgencyResponsible());
        assertEquals("Will investigate and remediate within 2 weeks", updatedServiceRequestDTO.getStatusNotes());
        assertEquals(ServiceRequestPriority.HIGH, updatedServiceRequestDTO.getPriority());
        assertEquals(ServiceRequestStatus.IN_PROGRESS, updatedServiceRequestDTO.getStatus());

        // update dates
        request = HttpRequest
                .PATCH("/jurisdiction-admin/requests/" + postResponseServiceRequestDTO.getId()+"?jurisdiction_id=fakecity.gov",
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
        response = createServiceRequest("001", "12345 Nearway",
                Map.of("attribute[SDWLK]", "CRACKED"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "6789 Faraway",
                Map.of("attribute[SDWLK]", "NARROW"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        HttpRequest<?> request = HttpRequest.GET("/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov")
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
        try (CSVReader reader = new CSVReader(new StringReader(text))) {
            reader.skip(1); // skip header
            String[] cells = reader.readNext();
            assertTrue(cells.length > 1);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void theCSVFileShouldNotContainCellsBeginningWithUnsafeCharacters() throws IOException {
        HttpResponse<?> response;

        // create service requests
        response = createServiceRequest("001", "=1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "@1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "+1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "-1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "\t1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        response = createServiceRequest("001", "\r1+3",
                Map.of("attribute[SDWLK]", "HEAVED_UNEVEN"), "fakecity.gov");
        assertEquals(HttpStatus.OK, response.getStatus());

        HttpRequest<?> request = HttpRequest.GET("/jurisdiction-admin/requests/download?jurisdiction_id=fakecity.gov")
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

    private HttpResponse<?> createGroup(String name, String jurisdictionId) {
        CreateUpdateGroupDTO groupDTO = new CreateUpdateGroupDTO();
        groupDTO.setName(name);
        HttpRequest<?> request = HttpRequest.POST("/jurisdiction-admin/groups?jurisdiction_id="+jurisdictionId, groupDTO)
                .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, GroupDTO[].class);
    }

    private HttpResponse<?> createService(String code, String name, String jurisdictionId, Long groupId) {
        return createService(code,name, jurisdictionId, groupId, null);
    }

    private HttpResponse<?> createService(String code, String name, String jurisdictionId, Long groupId, Integer orderPosition) {
        CreateServiceDTO serviceDTO = new CreateServiceDTO();
        serviceDTO.setServiceCode(code);
        serviceDTO.setServiceName(name);
        serviceDTO.setGroupId(groupId);
        serviceDTO.setOrderPosition(orderPosition);
        HttpRequest<?> request = HttpRequest.POST("/jurisdiction-admin/services?jurisdiction_id="+jurisdictionId, serviceDTO)
                .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, ServiceDTO.class);
    }

    private HttpResponse<?> addServiceDefinitionAttribute(Long serviceId, String jurisdictionId, CreateServiceDefinitionAttributeDTO serviceDefinitionAttributeDTO) {
        HttpRequest<?> request = HttpRequest.POST("/jurisdiction-admin/services/"+serviceId+"/attributes?jurisdiction_id="+jurisdictionId, serviceDefinitionAttributeDTO)
                .header("Authorization", "Bearer token.text.here");
        return client.toBlocking().exchange(request, ServiceDefinitionDTO.class);
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
}
