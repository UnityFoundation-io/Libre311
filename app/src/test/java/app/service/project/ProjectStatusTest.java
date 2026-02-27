package app.service.project;

import app.dto.project.ProjectDTO;
import app.dto.project.UpdateProjectDTO;
import app.dto.servicerequest.PatchServiceRequestDTO;
import app.dto.servicerequest.SensitiveServiceRequestDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.project.Project;
import app.model.project.ProjectRepository;
import app.model.project.ProjectStatus;
import app.model.service.Service;
import app.model.service.ServiceRepository;
import app.model.service.group.ServiceGroup;
import app.model.service.group.ServiceGroupRepository;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestRepository;
import app.service.geometry.LibreGeometryFactory;
import app.service.servicerequest.ServiceRequestService;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ProjectStatusTest {

    @Inject
    ProjectService projectService;

    @Inject
    ServiceRequestService serviceRequestService;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ServiceRequestRepository serviceRequestRepository;

    @Inject
    ServiceRepository serviceRepository;

    @Inject
    ServiceGroupRepository serviceGroupRepository;

    @Inject
    LibreGeometryFactory geometryFactory;

    @Inject
    DbCleanup dbCleanup;

    private Jurisdiction jurisdiction;
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
    }

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    private Project createProject(Instant startDate, Instant endDate) {
        Double[][] projectBounds = { {10.0, 10.0}, {10.0, 20.0}, {20.0, 20.0}, {20.0, 10.0}, {10.0, 10.0} };
        Project project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(projectBounds));
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        return projectRepository.save(project);
    }

    @Test
    void testProjectStatus_Open() {
        Instant now = Instant.now();
        Project project = createProject(now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));
        
        assertEquals(ProjectStatus.OPEN, project.getStatus());
        
        ProjectDTO dto = new ProjectDTO(project);
        assertEquals(ProjectStatus.OPEN, dto.getStatus());
    }

    @Test
    void testProjectStatus_ClosedByEndDate() {
        Instant now = Instant.now();
        Project project = createProject(now.minus(2, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS));
        
        assertEquals(ProjectStatus.CLOSED, project.getStatus());
        
        ProjectDTO dto = new ProjectDTO(project);
        assertEquals(ProjectStatus.CLOSED, dto.getStatus());
    }

    @Test
    void testProjectStatus_ClosedByDate() {
        Instant now = Instant.now();
        Project project = createProject(now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));
        project.setClosedDate(now);
        projectRepository.update(project);
        
        assertEquals(ProjectStatus.CLOSED, project.getStatus());
        
        ProjectDTO dto = new ProjectDTO(project);
        assertEquals(ProjectStatus.CLOSED, dto.getStatus());
    }

    @Test
    void testUpdateProject_CloseProject() {
        Instant now = Instant.now();
        Project project = createProject(now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));
        assertEquals(ProjectStatus.OPEN, project.getStatus());

        UpdateProjectDTO updateDTO = new UpdateProjectDTO();
        updateDTO.setClosedDate(now);

        ProjectDTO updated = projectService.updateProject(project.getId(), updateDTO, jurisdiction.getId());
        
        assertNotNull(updated.getClosedDate());
        assertEquals(ProjectStatus.CLOSED, updated.getStatus());
    }

    @Test
    void testRetroactiveAssociation() {
        Instant now = Instant.now();
        Project project = createProject(now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));

        // Create a service request with no project
        ServiceRequest sr = new ServiceRequest();
        sr.setService(service);
        sr.setJurisdiction(jurisdiction);
        sr.setLocation(geometryFactory.createPoint("15.0", "15.0"));
        sr.setAddressString("123 Test St");
        sr = serviceRequestRepository.save(sr);
        assertNull(sr.getProject());

        // Retroactively associate
        PatchServiceRequestDTO patchDTO = new PatchServiceRequestDTO();
        patchDTO.setProjectId(project.getId());

        SensitiveServiceRequestDTO updated = serviceRequestService.updateServiceRequest(sr.getId(), patchDTO, jurisdiction.getId());
        
        assertNotNull(updated.getProjectId());
        assertEquals(project.getId(), updated.getProjectId());

        // Remove association
        patchDTO.setProjectId(-1L);
        updated = serviceRequestService.updateServiceRequest(sr.getId(), patchDTO, jurisdiction.getId());
        assertNull(updated.getProjectId());
    }
}
