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

import app.dto.project.CreateProjectDTO;
import app.dto.project.ProjectDTO;
import app.dto.project.UpdateProjectDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.project.Project;
import app.model.project.ProjectRepository;
import app.security.HasPermissionResponse;
import app.service.geometry.LibreGeometryFactory;
import app.util.DbCleanup;
import app.util.MockAuthenticationFetcher;
import app.util.MockUnityAuthClient;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static app.util.MockAuthenticationFetcher.DEFAULT_MOCK_AUTHENTICATION;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(transactional = false)
public class ProjectAdminControllerTest {

    @Inject
    @Client("/api/jurisdiction-admin/projects")
    HttpClient client;

    @Inject
    MockUnityAuthClient mockUnityAuthClient;

    @Inject
    MockAuthenticationFetcher mockAuthenticationFetcher;

    @Inject
    DbCleanup dbCleanup;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    LibreGeometryFactory geometryFactory;

    private Jurisdiction jurisdiction;

    @BeforeEach
    void setup() {
        jurisdiction = new Jurisdiction("test-jurisdiction", 1L);
        jurisdictionRepository.save(jurisdiction);
        mockAuthenticationFetcher.setAuthentication(null);
    }

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    private void authLogin() {
        mockAuthenticationFetcher.setAuthentication(DEFAULT_MOCK_AUTHENTICATION);
        mockUnityAuthClient.setResponse(HttpResponse.ok(
                new HasPermissionResponse(true, "admin@test.com", null, List.of("LIBRE311_ADMIN_VIEW-TENANT", "LIBRE311_ADMIN_EDIT-TENANT"))));
    }

    @Test
    void testIndexUnauthenticated() {
        HttpClientResponseException exception = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("?jurisdiction_id=" + jurisdiction.getId()));
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void testIndexAuthenticated() {
        authLogin();
        
        Project project = new Project();
        project.setName("Test Project");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        projectRepository.save(project);

        HttpResponse<List<ProjectDTO>> response = client.toBlocking().exchange(
                HttpRequest.GET("?jurisdiction_id=" + jurisdiction.getId())
                        .header("Authorization", "Bearer token"), Argument.listOf(ProjectDTO.class));
        
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertFalse(response.body().isEmpty());
    }

    @Test
    void testCreateProject() {
        authLogin();

        CreateProjectDTO dto = new CreateProjectDTO();
        dto.setName("New Project");
        dto.setBounds(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}});
        dto.setStartDate(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        dto.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS));

        HttpResponse<ProjectDTO> response = client.toBlocking().exchange(
                HttpRequest.POST("?jurisdiction_id=" + jurisdiction.getId(), dto)
                        .header("Authorization", "Bearer token"), ProjectDTO.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        ProjectDTO body = response.body();
        assertNotNull(body);
        assertEquals("New Project", body.getName());
    }

    @Test
    void testUpdateProject() {
        authLogin();

        Project project = new Project();
        project.setName("Old Name");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        project = projectRepository.save(project);

        UpdateProjectDTO dto = new UpdateProjectDTO();
        dto.setName("Updated Name");

        HttpResponse<ProjectDTO> response = client.toBlocking().exchange(
                HttpRequest.PATCH("/" + project.getId() + "?jurisdiction_id=" + jurisdiction.getId(), dto)
                        .header("Authorization", "Bearer token"), ProjectDTO.class);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Updated Name", response.body().getName());
    }

    @Test
    void testDeleteProject() {
        authLogin();

        Project project = new Project();
        project.setName("To Delete");
        project.setJurisdiction(jurisdiction);
        project.setBoundary(geometryFactory.createPolygon(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}}));
        project.setStartDate(Instant.now());
        project.setEndDate(Instant.now().plus(7, ChronoUnit.DAYS));
        project = projectRepository.save(project);

        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.DELETE("/" + project.getId() + "?jurisdiction_id=" + jurisdiction.getId())
                        .header("Authorization", "Bearer token"));

        assertEquals(HttpStatus.OK, response.getStatus());
        assertFalse(projectRepository.findById(project.getId()).isPresent());
    }
}
