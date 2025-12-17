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

package app.servicerequest;

import app.jurisdiction.Jurisdiction;
import app.jurisdiction.JurisdictionRepository;
import app.servicedefinition.ServiceDefinition;
import app.servicedefinition.ServiceRepository;
import app.servicedefinition.group.ServiceGroup;
import app.servicedefinition.group.ServiceGroupRepository;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

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
    private ServiceDefinition testServiceDefinition;
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
        testServiceDefinition = new ServiceDefinition("Test Service");
        testServiceDefinition.setJurisdiction(testJurisdiction);
        testServiceDefinition.setServiceGroup(testServiceGroup);
        testServiceDefinition = serviceRepository.save(testServiceDefinition);
    }

    private ServiceRequest createTestServiceRequest() throws ParseException {
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
        ServiceRequest serviceRequest = createTestServiceRequest();
        assertFalse(serviceRequest.isDeleted());

        // When
        int updatedCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());

        // Then
        assertEquals(1, updatedCount);
        Optional<ServiceRequest> deletedServiceRequestOptional = serviceRequestRepository.findById(serviceRequest.getId());
        assertTrue(deletedServiceRequestOptional.isPresent());
        assertTrue(deletedServiceRequestOptional.get().isDeleted());

        // Verify it's not found by findByIdAndJurisdictionId (which filters for deleted=false)
        Optional<ServiceRequest> notFoundServiceRequest = serviceRequestRepository.findByIdAndJurisdictionId(serviceRequest.getId(), JURISDICTION_ID);
        assertTrue(notFoundServiceRequest.isEmpty());
    }

    @Test
    void delete_shouldReturnZeroWhenServiceRequestIsAlreadyDeleted() throws ParseException {
        // Given
        ServiceRequest serviceRequest = createTestServiceRequest();
        assertFalse(serviceRequest.isDeleted());

        // First delete
        int firstUpdateCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());
        assertEquals(1, firstUpdateCount);
        Optional<ServiceRequest> deletedServiceRequestOptional = serviceRequestRepository.findById(serviceRequest.getId());
        assertTrue(deletedServiceRequestOptional.isPresent());
        assertTrue(deletedServiceRequestOptional.get().isDeleted());

        // When
        int secondUpdateCount = serviceRequestService.delete(serviceRequest.getId(), testJurisdiction.getId());

        // Then
        assertEquals(0, secondUpdateCount); // Should return 0 because no non-deleted record was found/updated
    }

}