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

import app.model.jurisdiction.JurisdictionRepository;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class JurisdictionService {
    private static final Logger LOG = LoggerFactory.getLogger(JurisdictionService.class);
    private final JurisdictionRepository jurisdictionRepository;

    public JurisdictionService(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public List<Map> validateJurisdictionSupport(String jurisdictionId) {
        if (jurisdictionRepository.count() == 0) {
            return List.of();
        }

        if (jurisdictionId == null) {
            return List.of(Map.of(
                    "code", "400",
                    "description", "jurisdiction_id was not provided."
            ));
        }

        if (jurisdictionRepository.findById(jurisdictionId).isEmpty()) {
            return List.of(Map.of(
                    "code", "404",
                    "description", "jurisdiction_id not found."
            ));
        }

        return List.of();
    }
}
