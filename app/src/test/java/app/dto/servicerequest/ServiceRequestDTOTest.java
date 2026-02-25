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

package app.dto.servicerequest;

import app.model.jurisdiction.Jurisdiction;
import app.model.project.Project;
import app.model.service.Service;
import app.model.servicerequest.ServiceRequest;
import app.service.geometry.LibreGeometryFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceRequestDTOTest {

    @Test
    void testServiceRequestDTOConstructor() {
        Jurisdiction jurisdiction = new Jurisdiction("test-j", 1L);
        Service service = new Service("Test Service");
        service.setJurisdiction(jurisdiction);
        
        Project project = new Project();
        project.setId(100L);
        project.setName("Test Project");
        
        ServiceRequest request = new ServiceRequest();
        request.setId(200L);
        request.setService(service);
        request.setJurisdiction(jurisdiction);
        request.setProject(project);
        request.setLocation(new LibreGeometryFactory().createPoint("0.0", "0.0"));
        
        ServiceRequestDTO dto = new ServiceRequestDTO(request);

        assertEquals(200L, dto.getId());
        assertEquals(100L, dto.getProjectId());
    }
}
