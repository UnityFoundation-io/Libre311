package app.model.project;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.service.geometry.LibreGeometryFactory;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ProjectRepositoryTest {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    LibreGeometryFactory geometryFactory;

    @Inject
    DbCleanup dbCleanup;

    private Jurisdiction jurisdiction;

    @BeforeEach
    void setup() {
        jurisdiction = new Jurisdiction("test-jurisdiction", 1L);
        jurisdictionRepository.save(jurisdiction);
    }

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    @Test
    void testFindProjectForLocationAndTime() {
        // Create a project from (0,0) to (10,10)
        Double[][] bounds = {
            {0.0, 0.0},
            {0.0, 10.0},
            {10.0, 10.0},
            {10.0, 0.0},
            {0.0, 0.0}
        };

        Project project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(bounds));
        
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        project.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project.setEndDate(now.plus(1, ChronoUnit.DAYS));
        
        projectRepository.save(project);

        // Point at (5,5) should be inside
        Point location = geometryFactory.createPoint("5.0", "5.0");
        
        Optional<Project> found = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, now);
        assertTrue(found.isPresent());
        assertEquals("Test Project", found.get().getName());
    }

    @Test
    void testFindProjectForLocationAndTime_Outside() {
        // Create a project from (0,0) to (10,10)
        Double[][] bounds = {
            {0.0, 0.0},
            {0.0, 10.0},
            {10.0, 10.0},
            {10.0, 0.0},
            {0.0, 0.0}
        };

        Project project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(bounds));
        
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        project.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project.setEndDate(now.plus(1, ChronoUnit.DAYS));
        
        projectRepository.save(project);

        // Point at (15,15) should be outside
        Point location = geometryFactory.createPoint("15.0", "15.0");
        
        Optional<Project> found = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, now);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindProjectForLocationAndTime_MultipleOverlapping() {
        // Create two overlapping projects from (0,0) to (10,10)
        Double[][] bounds = {
            {0.0, 0.0},
            {0.0, 10.0},
            {10.0, 10.0},
            {10.0, 0.0},
            {0.0, 0.0}
        };

        Project project1 = new Project();
        project1.setName("Project 1");
        project1.setJurisdiction(jurisdiction);
        project1.setBoundary(geometryFactory.createPolygon(bounds));
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        project1.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project1.setEndDate(now.plus(1, ChronoUnit.DAYS));
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Project 2");
        project2.setJurisdiction(jurisdiction);
        project2.setBoundary(geometryFactory.createPolygon(bounds));
        project2.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project2.setEndDate(now.plus(1, ChronoUnit.DAYS));
        projectRepository.save(project2);

        // Point at (5,5) should be inside both.
        Point location = geometryFactory.createPoint("5.0", "5.0");
        
        Optional<Project> found = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, now);
        assertTrue(found.isPresent());
    }

    @Test
    void testFindProjectForLocationAndTime_OnBoundary() {
        // Create a project from (0,0) to (10,10)
        Double[][] bounds = {
            {0.0, 0.0},
            {0.0, 10.0},
            {10.0, 10.0},
            {10.0, 0.0},
            {0.0, 0.0}
        };

        Project project = new Project();
        project.setName("Boundary Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(bounds));
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        project.setStartDate(now.minus(1, ChronoUnit.DAYS));
        project.setEndDate(now.plus(1, ChronoUnit.DAYS));
        projectRepository.save(project);

        // Point exactly on the boundary (5, 0)
        Point location = geometryFactory.createPoint("5.0", "0.0");
        
        Optional<Project> found = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, now);
        assertTrue(found.isPresent(), "Project should be found for location on boundary due to intersects() query");
    }

    @Test
    void testFindProjectForLocationAndTime_TimeBounds() {
        Double[][] bounds = { {0.0, 0.0}, {0.0, 10.0}, {10.0, 10.0}, {10.0, 0.0}, {0.0, 0.0} };
        Project project = new Project();
        project.setName("Timed Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(bounds));
        
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        project.setStartDate(now.plus(1, ChronoUnit.DAYS)); // Starts tomorrow
        project.setEndDate(now.plus(2, ChronoUnit.DAYS));
        project = projectRepository.save(project);

        Point location = geometryFactory.createPoint("5.0", "5.0");
        
        // Should not be found now
        Optional<Project> foundNow = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, now);
        assertFalse(foundNow.isPresent());

        // Should be found at exactly start time
        Optional<Project> foundStart = projectRepository.findProjectForLocationAndTime(jurisdiction.getId(), location, project.getStartDate());
        assertTrue(foundStart.isPresent());
    }
}
