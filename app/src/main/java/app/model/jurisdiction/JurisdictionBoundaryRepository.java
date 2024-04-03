package app.model.jurisdiction;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface JurisdictionBoundaryRepository extends
    CrudRepository<JurisdictionBoundaryEntity, Long> {

    JurisdictionBoundaryEntity findByJurisdictionId(String jurisdictionId);
}
