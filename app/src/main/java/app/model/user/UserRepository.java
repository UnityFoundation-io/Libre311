package app.model.user;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public interface UserRepository extends PageableRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
