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

import app.model.jurisdiction.Jurisdiction;
import app.model.project.Project;
import app.service.geometry.LibreGeometryFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectDTOTest {

    @Test
    void testProjectDTOConstructor() {
        Jurisdiction jurisdiction = new Jurisdiction("test-jurisdiction", 1L);
        
        Project project = new Project();
        project.setId(100L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setJurisdiction(jurisdiction);
        project.setStartDate(Instant.parse("2023-01-01T00:00:00Z"));
        project.setEndDate(Instant.parse("2023-01-31T00:00:00Z"));
        
        Double[][] bounds = {{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}};
        project.setBoundary(new LibreGeometryFactory().createPolygon(bounds));

        ProjectDTO dto = new ProjectDTO(project);

        assertEquals(100L, dto.getId());
        assertEquals("Test Project", dto.getName());
        assertEquals("test-jurisdiction", dto.getJurisdictionId());
        assertEquals(Instant.parse("2023-01-01T00:00:00Z"), dto.getStartDate());
        assertEquals(Instant.parse("2023-01-31T00:00:00Z"), dto.getEndDate());
        assertEquals(5, dto.getBounds().length);
    }
}
