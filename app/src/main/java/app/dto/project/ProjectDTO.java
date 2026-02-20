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

package app.dto.project;

import app.model.project.Project;
import app.service.geometry.LibreGeometryFactory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

import java.time.Instant;

@Introspected
public class ProjectDTO {

    private Long id;

    private String name;

    private String description;

    private Double[][] bounds;

    @JsonProperty("start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant startDate;

    @JsonProperty("end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant endDate;

    @JsonProperty("jurisdiction_id")
    private String jurisdictionId;

    public ProjectDTO() {
    }

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.bounds = LibreGeometryFactory.getCoordinatesFrom(project.getBoundary());
        this.startDate = project.getStartDate();
        this.endDate = project.getEndDate();
        if (project.getJurisdiction() != null) {
            this.jurisdictionId = project.getJurisdiction().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Double[][] getBounds() {
        return bounds;
    }

    public void setBounds(Double[][] bounds) {
        this.bounds = bounds;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }
}
