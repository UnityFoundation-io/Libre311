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
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class JurisdictionService {
    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionService.class);
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
        jurisdiction.setPrimaryColor(requestDTO.getPrimaryColor());
        jurisdiction.setPrimaryHoverColor(requestDTO.getPrimaryHoverColor());
        jurisdiction.setLogoMediaUrl(requestDTO.getLogoMediaUrl());

        return new JurisdictionDTO(jurisdictionRepository.save(jurisdiction));
    }

    public JurisdictionDTO updateJurisdiction(String jurisdictionId, PatchJurisdictionDTO requestDTO) {
        Optional<Jurisdiction> jurisdictionOptional = jurisdictionRepository.findById(jurisdictionId);

        if (jurisdictionOptional.isEmpty()) {
            LOG.error("Could not find Jurisdiction with id {}.", jurisdictionId);
            return null;
        }

        Jurisdiction jurisdiction = jurisdictionOptional.get();
        applyPatch(requestDTO, jurisdiction);

        return new JurisdictionDTO(jurisdictionRepository.update(jurisdiction));
    }

    private static void applyPatch(PatchJurisdictionDTO jurisdictionDTO, Jurisdiction jurisdiction) {
        if (jurisdictionDTO.getName() != null) {
            jurisdiction.setName(jurisdictionDTO.getName());
        }
        if (jurisdictionDTO.getPrimaryColor() != null) {
            jurisdiction.setPrimaryColor(jurisdictionDTO.getPrimaryColor());
        }
        if (jurisdictionDTO.getPrimaryHoverColor() != null) {
            jurisdiction.setPrimaryHoverColor(jurisdictionDTO.getPrimaryHoverColor());
        }
        if (jurisdictionDTO.getLogoMediaUrl() != null) {
            jurisdiction.setLogoMediaUrl(jurisdictionDTO.getLogoMediaUrl());
        }
    }
}
