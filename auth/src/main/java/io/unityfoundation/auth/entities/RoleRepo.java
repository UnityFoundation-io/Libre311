package io.unityfoundation.auth.entities;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface RoleRepo extends CrudRepository<Role, Long> {
    Role findByName(String name);

    @Query("SELECT id FROM role")
    List<Long> findAllRoleIds();
}
