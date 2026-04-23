package io.unityfoundation.auth.entities;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface ServiceRepo extends CrudRepository<Service, Long> {

  @Query("""
      SELECT s.id, s.name, s.description, s.status
FROM tenant_service ts
         inner join service s on s.id = ts.service_id
where ts.status <> 'DISABLED'
  and ts.service_id = :serviceId
  and ts.tenant_id = :tenantId
     """)
  Optional<Service> findByTenantId(Long serviceId, Long tenantId);
}
