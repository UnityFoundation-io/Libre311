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

    public List<ProjectDTO> getProjects(String jurisdictionId) {
        return projectRepository.findAllByJurisdictionId(jurisdictionId).stream()
                .map(ProjectDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectDTO dto, String jurisdictionId) {
        Jurisdiction jurisdiction = jurisdictionRepository.findById(jurisdictionId)
                .orElseThrow(() -> new Libre311BaseException("Jurisdiction not found", HttpStatus.NOT_FOUND));

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

        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getBounds() != null) project.setBoundary(geometryFactory.createPolygon(dto.getBounds()));
        if (dto.getStartDate() != null) project.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) project.setEndDate(dto.getEndDate());
        if (dto.getClosedDate() != null) project.setClosedDate(dto.getClosedDate());

        return new ProjectDTO(projectRepository.update(project));
    }

    @Transactional
    public void deleteProject(Long id, String jurisdictionId) {
        Project project = projectRepository.findByIdAndJurisdictionId(id, jurisdictionId)
                .orElseThrow(() -> new Libre311BaseException("Project not found", HttpStatus.NOT_FOUND));
        projectRepository.delete(project);
    }

    public Optional<Project> findProjectForLocationAndTime(Point location, Instant time, String jurisdictionId) {
        return projectRepository.findProjectForLocationAndTime(jurisdictionId, location, time);
    }
}
