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

package app.service.project;

import app.dto.project.CreateProjectDTO;
import app.dto.project.ProjectDTO;
import app.dto.project.UpdateProjectDTO;
import app.exception.Libre311BaseException;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.project.Project;
import app.model.project.ProjectRepository;
import app.service.geometry.LibreGeometryFactory;
import app.util.DbCleanup;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ProjectServiceTest {

    @Inject
    ProjectService projectService;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    JurisdictionRepository jurisdictionRepository;

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
    void testCreateProject() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("New Project");
        dto.setDescription("Description");
        dto.setBounds(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}});
        dto.setStartDate(Instant.now());
        dto.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));

        ProjectDTO result = projectService.createProject(dto, jurisdiction.getId());

        assertNotNull(result.getId());
        assertEquals("New Project", result.getName());
        assertEquals("test-jurisdiction", result.getJurisdictionId());
        
        assertTrue(projectRepository.findById(result.getId()).isPresent());
    }

    @Test
    void testCreateProject_JurisdictionNotFound() {
        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("New Project");
        dto.setBounds(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}});
        dto.setStartDate(Instant.now());
        dto.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));

        Libre311BaseException exception = assertThrows(Libre311BaseException.class, () -> {
            projectService.createProject(dto, "non-existent");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Jurisdiction not found", exception.getMessage());
    }

    @Test
    void testUpdateProject() {
        Project project = new Project();
        project.setName("Old Name");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(new LibreGeometryFactory().createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        project = projectRepository.save(project);

        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("New Name");

        ProjectDTO result = projectService.updateProject(project.getId(), dto, jurisdiction.getId());

        assertEquals("New Name", result.getName());
        assertEquals(project.getId(), result.getId());
    }

    @Test
    void testUpdateProject_NotFound() {
        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("New Name");

        Libre311BaseException exception = assertThrows(Libre311BaseException.class, () -> {
            projectService.updateProject(999L, dto, jurisdiction.getId());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void testDeleteProject() {
        Project project = new Project();
        project.setName("To Delete");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(new LibreGeometryFactory().createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        project = projectRepository.save(project);

        projectService.deleteProject(project.getId(), jurisdiction.getId());

        assertFalse(projectRepository.findById(project.getId()).isPresent());
    }

    @Test
    void testGetProjects() {
        Project p1 = new Project();
        p1.setName("P1");
        p1.setJurisdiction(jurisdiction);
        p1.setBoundary(new LibreGeometryFactory().createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        p1.setStartDate(Instant.now());
        p1.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        projectRepository.save(p1);

        List<ProjectDTO> projects = projectService.getProjects(jurisdiction.getId());
        assertEquals(1, projects.size());
        assertEquals("P1", projects.get(0).getName());
    }
}
