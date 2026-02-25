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

package app.service.jurisdiction;

import app.dto.jurisdiction.CreateJurisdictionDTO;
import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.jurisdiction.PatchJurisdictionDTO;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdiction.ProjectFeature;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactional = false)
class JurisdictionServiceProjectFeatureTest {

    @Inject
    JurisdictionService jurisdictionService;

    @Inject
    JurisdictionRepository jurisdictionRepository;

    @Inject
    DbCleanup dbCleanup;

    @AfterEach
    void cleanup() {
        dbCleanup.cleanupAll();
    }

    @Test
    void shouldSetProjectFeatureOnCreate() {
        CreateJurisdictionDTO dto = new CreateJurisdictionDTO();
        dto.setJurisdictionId("test-j");
        dto.setName("Test");
        dto.setBounds(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}});
        dto.setProjectFeature(ProjectFeature.REQUIRED);

        JurisdictionDTO result = jurisdictionService.createJurisdiction(dto, 1L);
        assertEquals(ProjectFeature.REQUIRED, result.getProjectFeature());
    }

    @Test
    void shouldUpdateProjectFeatureOnPatch() {
        CreateJurisdictionDTO createDto = new CreateJurisdictionDTO();
        createDto.setJurisdictionId("test-j");
        createDto.setName("Test");
        createDto.setBounds(new Double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}, {1.0, 0.0}, {0.0, 0.0}});
        createDto.setProjectFeature(ProjectFeature.DISABLED);
        jurisdictionService.createJurisdiction(createDto, 1L);

        PatchJurisdictionDTO patchDto = new PatchJurisdictionDTO();
        patchDto.setProjectFeature(ProjectFeature.OPTIONAL);

        JurisdictionDTO result = jurisdictionService.updateJurisdiction("test-j", patchDto);
        assertEquals(ProjectFeature.OPTIONAL, result.getProjectFeature());
    }
}
