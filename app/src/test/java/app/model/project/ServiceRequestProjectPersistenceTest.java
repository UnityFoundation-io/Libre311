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

package app.model.project;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.service.geometry.LibreGeometryFactory;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
class ServiceRequestProjectPersistenceTest {

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    LibreGeometryFactory geometryFactory;

    @Inject
    DbCleanup dbCleanup;

    private Jurisdiction jurisdiction;
    private Project project;
    private Service service;

    @BeforeEach
    void setup() {
        jurisdiction = new Jurisdiction("test-jurisdiction", 1L);
        jurisdictionRepository.save(jurisdiction);

        ServiceGroup group = new ServiceGroup("Test Group", jurisdiction);
        serviceGroupRepository.save(group);

        service = new Service("Test Service");
        service.setJurisdiction(jurisdiction);
        service.setServiceGroup(group);
        serviceRepository.save(service);

        Double[][] bounds = {{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}};
        project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(bounds));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plusSeconds(3600));
        projectRepository.save(project);
    }

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    @Test
    void shouldPersistServiceRequestWithProject() {
        ServiceRequest request = new ServiceRequest();
        request.setService(service);
        request.setJurisdiction(jurisdiction);
        request.setProject(project);
        request.setLocation(geometryFactory.createPoint("0.5", "0.5"));
        
        ServiceRequest savedRequest = serviceRequestRepository.save(request);
        assertNotNull(savedRequest.getId());
        
        Optional<ServiceRequest> foundRequest = serviceRequestRepository.findById(savedRequest.getId());
        assertTrue(foundRequest.isPresent());
        assertNotNull(foundRequest.get().getProject());
        assertEquals(project.getId(), foundRequest.get().getProject().getId());
    }
}
