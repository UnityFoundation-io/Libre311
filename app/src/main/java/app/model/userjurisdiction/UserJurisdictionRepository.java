package app.model.userjurisdiction;

import app.model.user.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface UserJurisdictionRepository extends CrudRepository<UserJurisdiction, Long> {
    Optional<UserJurisdiction> findByUserAndJurisdictionId(User user, String jurisdictionId);
}
