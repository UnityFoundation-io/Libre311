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

import app.dto.jurisdiction.LatLongDTO;
import app.dto.jurisdiction.CreateJurisdictionDTO;
import app.dto.jurisdiction.JurisdictionDTO;
import app.dto.jurisdiction.PatchJurisdictionDTO;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.model.jurisdiction.LatLong;
import app.model.jurisdiction.LatLongRepository;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class JurisdictionService {

    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionService.class);

    @Property(name = "micronaut.http.services.auth.url")
    protected String authUrl;

    private final JurisdictionRepository jurisdictionRepository;
    private final LatLongRepository latLongRepository;

    public JurisdictionService(JurisdictionRepository jurisdictionRepository, LatLongRepository latLongRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
        this.latLongRepository = latLongRepository;
    }

    public JurisdictionDTO findJurisdictionByHostName(String hostName) {
        return jurisdictionRepository.findByRemoteHostsNameEquals(hostName)
            .map(jurisdiction -> new JurisdictionDTO(jurisdiction, authUrl)).orElse(null);
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

        Jurisdiction savedJurisdiction = jurisdictionRepository.save(jurisdiction);

        Set<LatLongDTO> dtoBounds = requestDTO.getBounds();
        if (dtoBounds != null) {
            saveNewBounds(dtoBounds, savedJurisdiction);
        }

        return new JurisdictionDTO(jurisdictionRepository.update(savedJurisdiction));
    }

    public JurisdictionDTO updateJurisdiction(String jurisdictionId, PatchJurisdictionDTO requestDTO) {
        Optional<Jurisdiction> jurisdictionOptional = jurisdictionRepository.findById(jurisdictionId);

        if (jurisdictionOptional.isEmpty()) {
            LOG.error("Could not find Jurisdiction with id {}.", jurisdictionId);
            return null;
        }

        Jurisdiction jurisdiction = jurisdictionOptional.get();
        applyPatch(requestDTO, jurisdiction);

        Set<LatLongDTO> dtoBounds = requestDTO.getBounds();
        if (dtoBounds != null) {
            updateBounds(jurisdiction, dtoBounds);
        }

        return new JurisdictionDTO(jurisdictionRepository.update(jurisdiction));
    }

    @Transactional
    public void updateBounds(Jurisdiction jurisdiction, Set<LatLongDTO> dtoBounds) {
        latLongRepository.deleteAll(jurisdiction.getBounds());
        saveNewBounds(dtoBounds, jurisdiction);
    }

    private void saveNewBounds(Set<LatLongDTO> dtoBounds, Jurisdiction jurisdiction) {
        Set<LatLong> newBounds = dtoBounds.stream().map(latLongDTO -> new LatLong(latLongDTO.getLatitude(), latLongDTO.getLongitude(), jurisdiction)).collect(Collectors.toSet());
        Set<LatLong> savedBounds = new HashSet<>((List<LatLong>) latLongRepository.saveAll(newBounds));
        jurisdiction.setBounds(savedBounds);
    }

    private void applyPatch(PatchJurisdictionDTO jurisdictionDTO, Jurisdiction jurisdiction) {
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
