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
import app.model.jurisdiction.LatLong;
import app.model.jurisdiction.LatLongRepository;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
            .map(jurisdiction -> {
                JurisdictionDTO jurisdictionDTO = new JurisdictionDTO(jurisdiction, authUrl);

                List<LatLong> bounds = latLongRepository.findAllByJurisdiction(jurisdiction);
                jurisdictionDTO.setBounds(bounds.stream()
                        .sorted(Comparator.comparing(LatLong::getOrderPosition))
                        .map(latLong -> new Double[]{latLong.getLatitude(), latLong.getLongitude()})
                        .toArray(Double[][]::new));

                return jurisdictionDTO;
            }).orElse(null);
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

        List<LatLong> bounds = saveNewBounds(requestDTO.getBounds(), savedJurisdiction);
        JurisdictionDTO jurisdictionDTO = new JurisdictionDTO(jurisdictionRepository.update(savedJurisdiction));
        jurisdictionDTO.setBounds(bounds.stream()
                .sorted(Comparator.comparing(LatLong::getOrderPosition))
                .map(latLong -> new Double[]{latLong.getLatitude(), latLong.getLongitude()})
                .toArray(Double[][]::new));

        return jurisdictionDTO;
    }

    public JurisdictionDTO updateJurisdiction(String jurisdictionId, PatchJurisdictionDTO requestDTO) {
        Optional<Jurisdiction> jurisdictionOptional = jurisdictionRepository.findById(jurisdictionId);

        if (jurisdictionOptional.isEmpty()) {
            LOG.error("Could not find Jurisdiction with id {}.", jurisdictionId);
            return null;
        }

        Jurisdiction jurisdiction = jurisdictionOptional.get();
        applyPatch(requestDTO, jurisdiction);
        JurisdictionDTO jurisdictionDTO = new JurisdictionDTO(jurisdictionRepository.update(jurisdiction));

        Double[][] dtoBounds = requestDTO.getBounds();
        if (dtoBounds != null) {
            List<LatLong> savedBounds = updateBounds(jurisdiction, dtoBounds);
            jurisdictionDTO.setBounds(savedBounds.stream()
                    .sorted(Comparator.comparing(LatLong::getOrderPosition))
                    .map(latLong -> new Double[]{latLong.getLatitude(), latLong.getLongitude()})
                    .toArray(Double[][]::new));
        }

        return jurisdictionDTO;
    }

    @Transactional
    public List<LatLong> updateBounds(Jurisdiction jurisdiction, Double[][] dtoBounds) {
        latLongRepository.deleteAll(latLongRepository.findAllByJurisdiction(jurisdiction));
        return saveNewBounds(dtoBounds, jurisdiction);
    }

    private List<LatLong> saveNewBounds(Double[][] dtoBounds, Jurisdiction jurisdiction) {
        List<LatLong> polygonalBound = checkValidPolygonBound(dtoBounds, jurisdiction);
        List<LatLong> savedPolygonalBound = (List<LatLong>) latLongRepository.saveAll(polygonalBound);
        jurisdiction.setBounds(savedPolygonalBound);

        return savedPolygonalBound;
    }

    private List<LatLong> checkValidPolygonBound(Double[][] dtoBounds, Jurisdiction jurisdiction) {

        if (Arrays.stream(dtoBounds).anyMatch(doubles -> doubles.length != 2)) {
            throw new IllegalArgumentException("Invalid polygonal bound - should consist of tuple decimals values only.");
        }

        Double[] firstTuple = dtoBounds[0];
        Double[] lastTuple = dtoBounds[dtoBounds.length - 1];
        if (!(firstTuple[0].equals(lastTuple[0]) && firstTuple[1].equals(lastTuple[1]))) {
            throw new IllegalArgumentException("Invalid polygonal bound - first element does not equal last.");
        }

        AtomicInteger pos = new AtomicInteger(-1);
        return Arrays.stream(dtoBounds)
                .map(latLongTuple -> {
                    pos.addAndGet(1);
                    return new LatLong(latLongTuple[0], latLongTuple[1], jurisdiction, pos.get());
                })
                .collect(Collectors.toList());
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
