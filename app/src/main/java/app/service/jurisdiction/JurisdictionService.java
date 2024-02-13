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
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import jakarta.inject.Singleton;

@Singleton
public class JurisdictionService {
    private final JurisdictionRepository jurisdictionRepository;

    public JurisdictionService(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public JurisdictionDTO findJurisdictionByHostName(String hostName) {
        return jurisdictionRepository.findByRemoteHostsNameEquals(hostName)
            .map(JurisdictionDTO::new).orElse(null);
    }

    public JurisdictionDTO createJurisdiction(CreateJurisdictionDTO requestDTO, Long tenantId) {
        if (jurisdictionRepository.existsById(requestDTO.getJurisdictionId())) {
            return null;
        }

        Jurisdiction jurisdiction = new Jurisdiction(requestDTO.getJurisdictionId(), tenantId);
        jurisdiction.setName(requestDTO.getName());

        return new JurisdictionDTO(jurisdictionRepository.save(jurisdiction));
    }
}
