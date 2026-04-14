package io.unityfoundation.auth.entities;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface TenantRepo extends CrudRepository<Tenant, Long> {

    @Query("""
SELECT t.*
FROM user_role ur
    INNER JOIN tenant t ON t.id = ur.tenant_id
    INNER JOIN user u ON u.id = ur.user_id
WHERE u.email = :email
    
""")
    List<Tenant> findAllByUserEmail(String email);
}
