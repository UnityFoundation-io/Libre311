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
import app.exception.Libre311BaseException;
import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionBoundary;
import app.model.jurisdiction.JurisdictionBoundaryEntity;
import app.model.jurisdiction.JurisdictionBoundaryRepository;
import app.model.jurisdiction.JurisdictionRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class JurisdictionService {

    static class JurisdictionNotFoundException extends Libre311BaseException {
        public JurisdictionNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }

        static JurisdictionNotFoundException noJurisdictionForHostname(String hostname){
            return new JurisdictionNotFoundException(
                String.format("No Jurisdiction found with hostname %s", hostname));
        }

        static JurisdictionNotFoundException noJurisdictionForId(String id){
            return new JurisdictionNotFoundException(
                String.format("No Jurisdiction found with id: %s", id));
        }

    }

    static class JurisdictionAlreadyExists extends Libre311BaseException {

        public JurisdictionAlreadyExists(String id) {
            super(String.format(
                    "Cannot create jurisdiction. A jurisdiction with id: %s already exists", id),
                HttpStatus.BAD_REQUEST);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionService.class);

    @Property(name = "micronaut.http.services.auth.url")
    protected String authUrl;

    private final JurisdictionRepository jurisdictionRepository;
    JurisdictionBoundaryRepository jurisdictionBoundaryRepository;
    JurisdictionBoundaryService jurisdictionBoundaryService;

    public JurisdictionService(JurisdictionRepository jurisdictionRepository,
        JurisdictionBoundaryRepository jurisdictionBoundaryRepository,
        JurisdictionBoundaryService jurisdictionBoundaryService) {
        this.jurisdictionRepository = jurisdictionRepository;
        this.jurisdictionBoundaryRepository = jurisdictionBoundaryRepository;
        this.jurisdictionBoundaryService = jurisdictionBoundaryService;
    }

    public JurisdictionDTO findJurisdictionByHostName(String hostName) {
        return jurisdictionRepository.findByRemoteHostsNameEquals(hostName)
            .map(jurisdiction -> {
                JurisdictionDTO jurisdictionDTO = new JurisdictionDTO(jurisdiction, authUrl);

                JurisdictionBoundaryEntity jurisdictionBoundary = jurisdictionBoundaryRepository.findByJurisdictionId(
                    jurisdiction.getId());
                jurisdictionDTO.setBounds(jurisdictionBoundary.getBoundary());

                return jurisdictionDTO;
            }).orElseThrow(() -> JurisdictionNotFoundException.noJurisdictionForHostname(hostName));
    }

    public JurisdictionDTO createJurisdiction(CreateJurisdictionDTO requestDTO, Long tenantId) {
        if (jurisdictionRepository.existsById(requestDTO.getJurisdictionId())) {
            throw new JurisdictionAlreadyExists(requestDTO.getJurisdictionId());
        }

        Jurisdiction jurisdiction = new Jurisdiction(requestDTO.getJurisdictionId(), tenantId);
        jurisdiction.setName(requestDTO.getName());
        jurisdiction.setPrimaryColor(requestDTO.getPrimaryColor());
        jurisdiction.setPrimaryHoverColor(requestDTO.getPrimaryHoverColor());
        jurisdiction.setLogoMediaUrl(requestDTO.getLogoMediaUrl());
        jurisdiction.setTermsOfUseContent(
            requestDTO.getTermsOfUseContent() != null && !requestDTO.getTermsOfUseContent().isEmpty()
                ? requestDTO.getTermsOfUseContent()
                : null
        );
        jurisdiction.setPrivacyPolicyContent(
            requestDTO.getPrivacyPolicyContent() != null && !requestDTO.getPrivacyPolicyContent().isEmpty()
                ? requestDTO.getPrivacyPolicyContent()
                : null
        );

        Jurisdiction savedJurisdiction = jurisdictionRepository.save(jurisdiction);
        JurisdictionBoundary savedBoundary = jurisdictionBoundaryService.saveBoundary(
            savedJurisdiction, requestDTO.getBounds());

        return new JurisdictionDTO(savedJurisdiction, savedBoundary);
    }

    public JurisdictionDTO updateJurisdiction(String jurisdictionId, PatchJurisdictionDTO requestDTO) {
        Optional<Jurisdiction> jurisdictionOptional = jurisdictionRepository.findById(jurisdictionId);

        if (jurisdictionOptional.isEmpty()){
            throw JurisdictionNotFoundException.noJurisdictionForId(jurisdictionId);
        }

        Jurisdiction jurisdiction = jurisdictionOptional.get();
        applyPatch(requestDTO, jurisdiction);
        JurisdictionDTO jurisdictionDTO = new JurisdictionDTO(jurisdictionRepository.update(jurisdiction));

        Double[][] dtoBounds = requestDTO.getBounds();
        if (dtoBounds != null) {
            JurisdictionBoundary savedBoundary = jurisdictionBoundaryService.updateBoundary(
                jurisdiction, dtoBounds);
            jurisdictionDTO.setBounds(savedBoundary.getBoundary());
        }
        return jurisdictionDTO;
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
        if (jurisdictionDTO.getTermsOfUseContent() != null) {
            jurisdiction.setTermsOfUseContent(
                jurisdictionDTO.getTermsOfUseContent().isEmpty() ? null : jurisdictionDTO.getTermsOfUseContent()
            );
        }
        if (jurisdictionDTO.getPrivacyPolicyContent() != null) {
            jurisdiction.setPrivacyPolicyContent(
                jurisdictionDTO.getPrivacyPolicyContent().isEmpty() ? null : jurisdictionDTO.getPrivacyPolicyContent()
            );
        }
    }
}
