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
import app.security.RequiresPermissions;
import app.service.project.ProjectService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.util.List;

import static app.security.Permission.*;

@Controller("/api/jurisdiction-admin/projects")
@Tag(name = "Project")
public class ProjectAdminController {

    private final ProjectService projectService;

    public ProjectAdminController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Get(uris = { "{?jurisdiction_id}", ".json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_VIEW_SYSTEM, LIBRE311_ADMIN_VIEW_TENANT, LIBRE311_ADMIN_VIEW_SUBTENANT})
    public List<ProjectDTO> index(@Nullable @QueryValue("jurisdiction_id") String jurisdictionId) {
        return projectService.getProjects(jurisdictionId);
    }

    @Post(uris = { "{?jurisdiction_id}", ".json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ProjectDTO create(@Valid @Body CreateProjectDTO requestDTO,
                             @Nullable @QueryValue("jurisdiction_id") String jurisdictionId) {
        return projectService.createProject(requestDTO, jurisdictionId);
    }

    @Patch(uris = { "/{id}{?jurisdiction_id}", "/{id}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public ProjectDTO update(Long id, @Valid @Body UpdateProjectDTO requestDTO,
                             @Nullable @QueryValue("jurisdiction_id") String jurisdictionId) {
        return projectService.updateProject(id, requestDTO, jurisdictionId);
    }

    @Delete(uris = { "/{id}{?jurisdiction_id}", "/{id}.json{?jurisdiction_id}" })
    @ExecuteOn(TaskExecutors.IO)
    @RequiresPermissions({LIBRE311_ADMIN_EDIT_SYSTEM, LIBRE311_ADMIN_EDIT_TENANT, LIBRE311_ADMIN_EDIT_SUBTENANT})
    public HttpResponse<?> delete(Long id, @Nullable @QueryValue("jurisdiction_id") String jurisdictionId) {
        projectService.deleteProject(id, jurisdictionId);
        return HttpResponse.ok();
    }
}
