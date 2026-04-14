package io.unityfoundation.auth.entities;


import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

import static io.unityfoundation.auth.PermissionsService.*;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface UserRepo extends CrudRepository<User, Long> {

  Optional<User> findByEmail(String email);

  @Query("""
    SELECT count(*) > 0
    FROM user_role ur
      inner join user u on u.id = ur.user_id
    WHERE u.email = :email
      and ur.tenant_id = :tenantId;
""")
  boolean existsByEmailAndTenantId(String email, Long tenantId);

  @Query("""
      SELECT count(*) > 0
FROM user_role ur
         inner join user u on u.id = ur.user_id
         inner join tenant t on t.id = ur.tenant_id
         inner join tenant_service ts on ts.tenant_id = t.id
         INNER join service s on s.id = ts.service_id
where u.id = :userId
  and t.status = 'ENABLED'
  and s.id = :serviceId
  and s.status = 'ENABLED';
""")
  Boolean isServiceAvailable(long userId, long serviceId);

  @Query("""
      SELECT id,
       password,
       email,
       first_name,
       last_name,
       status
FROM user
WHERE email = :email
""")
  Optional<User> findUserForAuthentication(String email);

  @Query("""
      select ur.tenant_id as tenant_id, p.name as permission_name, p.`scope` as permission_scope
from user_role ur
         inner join role_permission rp on rp.role_id = ur.role_id
         inner join permission p on p.id = rp.permission_id
where ur.user_id = :userId
""")
  List<TenantPermission> getTenantPermissionsFor(Long userId);

  @Query("""
    select u.*
    from user_role ur inner join user u on u.id = ur.user_id
    where ur.tenant_id = :tenantId""")
  List<User> findAllByTenantId(Long tenantId);

  @Query("""
select count(*) > 0
    from user_role ur
    inner join user u on u.id = ur.user_id
    inner join role r on r.id = ur.role_id
    where u.email = :email and ur.tenant_id = :tenantId and r.name = 'Tenant Administrator'
""")
  boolean existsByEmailAndTenantEqualsAndIsTenantAdmin(String email, Long tenantId);

  @Query("""
select count(*) > 0
    from user_role ur
    inner join user u on u.id = ur.user_id
    inner join role r on r.id = ur.role_id
    where u.email = :email and r.name = 'Unity Administrator'
""")
  boolean existsByEmailAndRoleEqualsUnityAdmin(String email);

  @Query("select role_id from user_role where user_id = :userId")
  List<Long> getUserRolesByUserId(Long userId);

  @Query("INSERT INTO user_role(user_id, tenant_id, role_id) VALUES (:userId, :tenantId, :roleId)")
  void insertUserRole(Long userId, Long tenantId, Long roleId);

  @Query("DELETE FROM user_role WHERE tenant_id = :tenantId and user_id = :userId")
  void deleteRoleByTenantIdAndUserId(Long tenantId, Long userId);
}
