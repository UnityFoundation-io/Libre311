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

package app.model.project;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByJurisdictionId(String jurisdictionId);

    Optional<Project> findByIdAndJurisdictionId(Long id, String jurisdictionId);

    @Query("FROM Project p WHERE p.jurisdiction.id = :jurisdictionId AND p.startDate <= :time AND p.endDate >= :time AND intersects(p.boundary, :location) = true")
    Optional<Project> findProjectForLocationAndTime(String jurisdictionId, Point location, Instant time);
}
