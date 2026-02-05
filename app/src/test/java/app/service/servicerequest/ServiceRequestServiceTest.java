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

package app.service.servicerequest;

import app.dto.servicerequest.PatchServiceRequestDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestRepository;
import app.model.servicerequest.ServiceRequestStatus;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
class ServiceRequestServiceTest {
    @Inject
    ServiceRequestService serviceRequestService;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    DbCleanup dbCleanup;

    private Jurisdiction testJurisdiction;
    private Service testServiceDefinition;
    private ServiceGroup testServiceGroup;

    private static final String JURISDICTION_ID = "test-jurisdiction";

    @BeforeEach
    void setup() throws ParseException {
        setupMockData();
    }

    @AfterEach
    void teardown() {
        dbCleanup.cleanupAll();
    }

    void setupMockData() {
        testJurisdiction = jurisdictionRepository.save(new Jurisdiction(JURISDICTION_ID, 1L));
        testServiceGroup = serviceGroupRepository.save(new ServiceGroup("Test Group", testJurisdiction));
        testServiceDefinition = new Service("Test Service");
        testServiceDefinition.setJurisdiction(testJurisdiction);
        testServiceDefinition.setServiceGroup(testServiceGroup);
        testServiceDefinition = serviceRepository.save(testServiceDefinition);
    }

    private ServiceRequest createPersistedTestServiceRequest() throws ParseException {
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setJurisdiction(testJurisdiction);
        serviceRequest.setService(testServiceDefinition);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setPriority(ServiceRequestPriority.LOW);
        Point point = (Point) new WKTReader().read("POINT (-104.9903 39.7392)");
        point.setSRID(4326);
        serviceRequest.setLocation(point);
        return serviceRequestRepository.save(serviceRequest);
    }

    @Test
    void delete_shouldReturnZeroWhenServiceRequestDoesNotExist() {
        // Given
        Long nonExistentId = -1L;

        // When
        int updatedCount = serviceRequestService.delete(nonExistentId, testJurisdiction.getId());

        // Then
        assertEquals(0, updatedCount);
    }

    @Test
    void delete_shouldMarkServiceRequestAsDeletedAndReturnOneOnSuccess() throws ParseException {
        // Given
        ServiceRequest serviceRequest = createPersistedTestServiceRequest();
        assertFalse(serviceRequest.isDeleted());

        // When
        int updatedCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());

        // Then
        assertEquals(1, updatedCount);
        Optional<ServiceRequest> deletedServiceRequestOptional = serviceRequestRepository.findByIdAndDeleted(serviceRequest.getId(), true);
        assertTrue(deletedServiceRequestOptional.isPresent());
        assertTrue(deletedServiceRequestOptional.get().isDeleted());

        // Verify it's not found by findByIdAndJurisdictionId (which filters for deleted=false)
        Optional<ServiceRequest> notFoundServiceRequest = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequest.getId(), JURISDICTION_ID);
        assertTrue(notFoundServiceRequest.isEmpty());
    }

    @Test
    void delete_shouldReturnZeroWhenServiceRequestIsAlreadyDeleted() throws ParseException {
        // Given
        ServiceRequest serviceRequest = createPersistedTestServiceRequest();
        assertFalse(serviceRequest.isDeleted());

        // First delete
        int firstUpdateCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());
        assertEquals(1, firstUpdateCount);
        Optional<ServiceRequest> deletedServiceRequestOptional = serviceRequestRepository.findByIdAndDeleted(serviceRequest.getId(), true);
        assertTrue(deletedServiceRequestOptional.isPresent());
        assertTrue(deletedServiceRequestOptional.get().isDeleted());

        // When
        int secondUpdateCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());

        // Then
        assertEquals(0, secondUpdateCount); // Should return 0 because no non-deleted record was found/updated
    }


    @Test
    void applyPatch_shouldUpdateStatus() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setStatus(ServiceRequestStatus.CLOSED);

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestStatus.CLOSED, serviceRequest.getStatus());
    }

    @Test
    void applyPatch_shouldUpdatePriority() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setPriority(ServiceRequestPriority.HIGH);

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestPriority.HIGH, serviceRequest.getPriority());
    }

    @Test
    void applyPatch_shouldUpdateAgencyEmail() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setAgencyEmail("new@agency.gov");

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals("new@agency.gov", serviceRequest.getAgencyEmail());
    }

    @Test
    void applyPatch_shouldUpdateServiceNotice() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setServiceNotice("Service notice updated");

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals("Service notice updated", serviceRequest.getServiceNotice());
    }

    @Test
    void applyPatch_shouldUpdateStatusNotes() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setStatusNotes("Work completed");

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals("Work completed", serviceRequest.getStatusNotes());
    }

    @Test
    void applyPatch_shouldUpdateAgencyResponsible() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setAgencyResponsible("Public Works Department");

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals("Public Works Department", serviceRequest.getAgencyResponsible());
    }

    @Test
    void applyPatch_shouldUpdateExpectedDate() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        Instant expectedDate = Instant.parse("2024-12-31T23:59:59Z");
        patchDTO.setExpectedDate(JsonNullable.of(expectedDate));

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals(expectedDate, serviceRequest.getExpectedDate());
    }

    @Test
    void applyPatch_shouldClearExpectedDate() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        serviceRequest.setExpectedDate(Instant.parse("2024-12-31T23:59:59Z"));
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setExpectedDate(JsonNullable.of(null));

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertNull(serviceRequest.getExpectedDate()); // Should clear existing value
    }

    @Test
    void applyPatch_shouldUpdateClosedDate() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        Instant closedDate = Instant.parse("2024-11-01T10:00:00Z");
        patchDTO.setClosedDate(closedDate);

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals(closedDate, serviceRequest.getClosedDate());
    }

    @Test
    void applyPatch_shouldUpdateMultipleFields() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setStatus(ServiceRequestStatus.IN_PROGRESS);
        patchDTO.setPriority(ServiceRequestPriority.MEDIUM);
        patchDTO.setStatusNotes("Under investigation");
        patchDTO.setAgencyResponsible("IT Department");

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        assertEquals(ServiceRequestStatus.IN_PROGRESS, serviceRequest.getStatus());
        assertEquals(ServiceRequestPriority.MEDIUM, serviceRequest.getPriority());
        assertEquals("Under investigation", serviceRequest.getStatusNotes());
        assertEquals("IT Department", serviceRequest.getAgencyResponsible());
    }

    @Test
    void applyPatch_shouldNotUpdateNullFields() {
        ServiceRequest serviceRequest = createTestServiceRequest();
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setPriority(ServiceRequestPriority.LOW);
        serviceRequest.setAgencyEmail("original@agency.gov");

        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        // Only set status, leave others null
        patchDTO.setStatus(ServiceRequestStatus.CLOSED);

        ServiceRequestService.applyPatch(patchDTO, serviceRequest);

        // Status should be updated
        assertEquals(ServiceRequestStatus.CLOSED, serviceRequest.getStatus());
        // Others should remain unchanged
        assertEquals(ServiceRequestPriority.LOW, serviceRequest.getPriority());
        assertEquals("original@agency.gov", serviceRequest.getAgencyEmail());
    }

    private ServiceRequest createTestServiceRequest() {
        ServiceRequest serviceRequest = new ServiceRequest();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("test-jurisdiction");
        Service service = new Service();
        service.setJurisdiction(jurisdiction);
        
        serviceRequest.setService(service);
        serviceRequest.setJurisdiction(jurisdiction);
        serviceRequest.setStatus(ServiceRequestStatus.OPEN);
        serviceRequest.setPriority(ServiceRequestPriority.LOW);
        
        return serviceRequest;
    }
}
