package app.service.servicerequest;

import app.dto.servicerequest.PostRequestServiceRequestDTO;
import app.dto.servicerequest.PostResponseServiceRequestDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdiction.ProjectFeature;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.project.Project;
import app.model.project.ProjectRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.service.geometry.LibreGeometryFactory;
import app.service.jurisdiction.JurisdictionBoundaryService;
import app.util.DbCleanup;
import io.micronaut.http.HttpRequest;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ProjectFeatureTest {

    @Inject
    ServiceRequestService serviceRequestService;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    LibreGeometryFactory geometryFactory;

    @Inject
    JurisdictionBoundaryService jurisdictionBoundaryService;

    @Inject
    DbCleanup dbCleanup;

    private Jurisdiction jurisdiction;
    private Service service;

    @BeforeEach
    void setup() {
        jurisdiction = new Jurisdiction("test-jurisdiction", 1L);
        jurisdiction.setProjectFeature(ProjectFeature.DISABLED);
        jurisdictionRepository.save(jurisdiction);

        // Save jurisdiction boundary
        Double[][] boundary = { {0.0, 0.0}, {0.0, 100.0}, {100.0, 100.0}, {100.0, 0.0}, {0.0, 0.0} };
        jurisdictionBoundaryService.saveBoundary(jurisdiction, boundary);

        ServiceGroup group = new ServiceGroup("Test Group", jurisdiction);
        serviceGroupRepository.save(group);

        service = new Service("Test Service");
        service.setJurisdiction(jurisdiction);
        service.setServiceGroup(group);
        serviceRepository.save(service);
    }

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    private Project createProject() {
        Double[][] projectBounds = { {10.0, 10.0}, {10.0, 20.0}, {20.0, 20.0}, {20.0, 10.0}, {10.0, 10.0} };
        Project project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(projectBounds));
        Instant now = Instant.now();
        project.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project.setEndDate(now.plus(1, ChronoUnit.DAYS));
        return projectRepository.save(project);
    }

    @Test
    void testCreateServiceRequest_ProjectRequired_Found() {
        jurisdiction.setProjectFeature(ProjectFeature.REQUIRED);
        jurisdictionRepository.update(jurisdiction);
        Project project = createProject();

        PostRequestServiceRequestDTO dto = new PostRequestServiceRequestDTO(service.getId());
        dto.setLatitude("15.0");
        dto.setLongitude("15.0");
        dto.setgRecaptchaResponse("dummy");

        PostResponseServiceRequestDTO response = serviceRequestService.createServiceRequest(HttpRequest.POST("/", ""), dto, jurisdiction.getId());
        
        assertNotNull(response);
        Optional<ServiceRequest> savedRequest = serviceRequestRepository.findById(response.getId());
        assertTrue(savedRequest.isPresent());
        assertNotNull(savedRequest.get().getProject());
        assertEquals(project.getId(), savedRequest.get().getProject().getId());
    }

    @Test
    void testCreateServiceRequest_ProjectRequired_NotFound() {
        jurisdiction.setProjectFeature(ProjectFeature.REQUIRED);
        jurisdictionRepository.update(jurisdiction);
        
        PostRequestServiceRequestDTO dto = new PostRequestServiceRequestDTO(service.getId());
        dto.setLatitude("50.0"); // Outside project bounds
        dto.setLongitude("50.0");
        dto.setgRecaptchaResponse("dummy");

        Exception exception = assertThrows(ServiceRequestService.InvalidServiceRequestException.class, () -> {
            serviceRequestService.createServiceRequest(HttpRequest.POST("/", ""), dto, jurisdiction.getId());
        });

        assertTrue(exception.getMessage().contains("active project boundaries"));
    }

    @Test
    void testCreateServiceRequest_ProjectOptional_Found() {
        jurisdiction.setProjectFeature(ProjectFeature.OPTIONAL);
        jurisdictionRepository.update(jurisdiction);
        Project project = createProject();

        PostRequestServiceRequestDTO dto = new PostRequestServiceRequestDTO(service.getId());
        dto.setLatitude("15.0");
        dto.setLongitude("15.0");
        dto.setgRecaptchaResponse("dummy");

        PostResponseServiceRequestDTO response = serviceRequestService.createServiceRequest(HttpRequest.POST("/", ""), dto, jurisdiction.getId());
        
        assertNotNull(response);
        Optional<ServiceRequest> savedRequest = serviceRequestRepository.findById(response.getId());
        assertTrue(savedRequest.isPresent());
        assertNotNull(savedRequest.get().getProject());
        assertEquals(project.getId(), savedRequest.get().getProject().getId());
    }

    @Test
    void testCreateServiceRequest_ProjectOptional_NotFound() {
        jurisdiction.setProjectFeature(ProjectFeature.OPTIONAL);
        jurisdictionRepository.update(jurisdiction);
        
        PostRequestServiceRequestDTO dto = new PostRequestServiceRequestDTO(service.getId());
        dto.setLatitude("50.0"); // Outside project bounds
        dto.setLongitude("50.0");
        dto.setgRecaptchaResponse("dummy");

        PostResponseServiceRequestDTO response = serviceRequestService.createServiceRequest(HttpRequest.POST("/", ""), dto, jurisdiction.getId());
        
        assertNotNull(response);
        Optional<ServiceRequest> savedRequest = serviceRequestRepository.findById(response.getId());
        assertTrue(savedRequest.isPresent());
        assertNull(savedRequest.get().getProject(), "Project should not be set if none found in OPTIONAL mode");
    }

    @Test
    void testCreateServiceRequest_ProjectDisabled_FoundInBounds() {
        jurisdiction.setProjectFeature(ProjectFeature.DISABLED);
        jurisdictionRepository.update(jurisdiction);
        Project project = createProject();

        PostRequestServiceRequestDTO dto = new PostRequestServiceRequestDTO(service.getId());
        dto.setLatitude("15.0");
        dto.setLongitude("15.0");
        dto.setgRecaptchaResponse("dummy");

        PostResponseServiceRequestDTO response = serviceRequestService.createServiceRequest(HttpRequest.POST("/", ""), dto, jurisdiction.getId());
        
        assertNotNull(response);
        Optional<ServiceRequest> savedRequest = serviceRequestRepository.findById(response.getId());
        assertTrue(savedRequest.isPresent());
        assertNull(savedRequest.get().getProject(), "Project should not be set if feature is DISABLED");
    }
}
