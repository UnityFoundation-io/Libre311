package app.model.userjurisdiction;

import app.model.user.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface UserJurisdictionRepository extends CrudRepository<UserJurisdiction, Long> {
    List<UserJurisdiction> findByUser(User user);
}
