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
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JurisdictionRepository jurisdictionRepository;
    private final LibreGeometryFactory geometryFactory;

    public ProjectService(ProjectRepository projectRepository,
                          JurisdictionRepository jurisdictionRepository,
                          LibreGeometryFactory geometryFactory) {
        this.projectRepository = projectRepository;
        this.jurisdictionRepository = jurisdictionRepository;
        this.geometryFactory = geometryFactory;
    }

    @Transactional
    public List<ProjectDTO> getProjects(String jurisdictionId) {
        return projectRepository.findAllByJurisdictionId(jurisdictionId).stream()
                .map(ProjectDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ProjectDTO> getOpenProjects(String jurisdictionId) {
        return projectRepository.findOpenProjectsByJurisdictionId(jurisdictionId).stream()
                .map(ProjectDTO::new)
                .collect(Collectors.toList());
    }

    public static String toSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }

    @Transactional
    public Optional<ProjectDTO> getProjectBySlug(String slug, String jurisdictionId) {
        return projectRepository.findBySlugAndJurisdictionId(slug, jurisdictionId)
                .map(ProjectDTO::new);
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectDTO dto, String jurisdictionId) {
        Jurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId)
                .orElseThrow(() -> new Libre311BaseException("Jurisdiction not found", HttpStatus.NOT_FOUND));

        // Uniqueness check: name must be unique during the project's duration
        boolean exists = projectRepository.findAllByJurisdictionId(jurisdictionId).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(dto.getName()) &&
                        !(dto.getEndDate().isBefore(p.getStartDate()) || dto.getStartDate().isAfter(p.getEndDate())));
        if (exists) {
            throw new Libre311BaseException("A project with this name already exists during the specified duration", HttpStatus.BAD_REQUEST);
        }

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setBoundary(geometryFactory.createPolygon(dto.getBounds()));
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setJurisdiction(jurisdiction);

        return new ProjectDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectDTO updateProject(Long id, UpdateProjectDTO dto, String jurisdictionId) {
        Project project = projectRepository.findByIdAndJurisdictionId(id, jurisdictionId)
                .orElseThrow(() -> new Libre311BaseException("Project not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(project.getName())) {
            Instant start = dto.getStartDate() != null ? dto.getStartDate() : project.getStartDate();
            Instant end = dto.getEndDate() != null ? dto.getEndDate() : project.getEndDate();
            boolean exists = projectRepository.findAllByJurisdictionId(jurisdictionId).stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(dto.getName()) &&
                            !(end.isBefore(p.getStartDate()) || start.isAfter(p.getEndDate())));
            if (exists) {
                throw new Libre311BaseException("A project with this name already exists during the specified duration", HttpStatus.BAD_REQUEST);
            }
            project.setName(dto.getName());
        } else if (dto.getStartDate() != null || dto.getEndDate() != null) {
            Instant start = dto.getStartDate() != null ? dto.getStartDate() : project.getStartDate();
            Instant end = dto.getEndDate() != null ? dto.getEndDate() : project.getEndDate();
            boolean exists = projectRepository.findAllByJurisdictionId(jurisdictionId).stream()
                    .anyMatch(p -> !p.getId().equals(id) && p.getName().equalsIgnoreCase(project.getName()) &&
                            !(end.isBefore(p.getStartDate()) || start.isAfter(p.getEndDate())));
            if (exists) {
                throw new Libre311BaseException("Changing the duration would cause a naming conflict with another project", HttpStatus.BAD_REQUEST);
            }
        }

        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getBounds() != null) project.setBoundary(geometryFactory.createPolygon(dto.getBounds()));
        if (dto.getStartDate() != null) project.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) project.setEndDate(dto.getEndDate());

        dto.getClosedDate().ifPresent(project::setClosedDate);

        return new ProjectDTO(projectRepository.update(project));
    }

    public Optional<Project> findProjectForLocationAndTime(Point location, Instant time, String jurisdictionId) {
        return projectRepository.findProjectForLocationAndTime(jurisdictionId, location, time);
    }
}
