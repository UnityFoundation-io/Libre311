package io.unityfoundation.auth;

import io.micronaut.core.annotation.Introspected;
import io.unityfoundation.auth.entities.Permission;
import io.unityfoundation.auth.entities.Tenant;
import io.unityfoundation.auth.entities.User;
import io.unityfoundation.auth.entities.UserRepo;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Singleton
public class PermissionsService {

    private final UserRepo userRepo;

    private final BiPredicate<TenantPermission, Tenant> isTenantOrSystemOrSubtenantScopeAndBelongsToTenant = (tp, t) ->
            Permission.PermissionScope.SYSTEM.equals(tp.permissionScope()) || (
                    (Permission.PermissionScope.TENANT.equals(tp.permissionScope())
                            || Permission.PermissionScope.SUBTENANT.equals(tp.permissionScope()))
                            && tp.tenantId == t.getId());

    private final Predicate<TenantPermission> isTenantOrSystemOrSubtenantScope = (tp) ->
            Permission.PermissionScope.SYSTEM.equals(tp.permissionScope()) || (
                    (Permission.PermissionScope.TENANT.equals(tp.permissionScope())
                            || Permission.PermissionScope.SUBTENANT.equals(tp.permissionScope())));

    public PermissionsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<String> checkUserPermission(User user, Tenant tenant, List<String> permissions) {
        return getPermissionsFor(user, tenant).stream()
                .filter(permissions::contains).toList();
    }

    public List<String> getPermissionsFor(User user, Tenant tenant) {
        return userRepo.getTenantPermissionsFor(user.getId()).stream()
                .filter(tenantPermission ->
                        isTenantOrSystemOrSubtenantScopeAndBelongsToTenant.test(tenantPermission, tenant))
                .map(TenantPermission::permissionName)
                .toList();
    }

    public List<String> checkUserPermissionsAcrossAllTenants(User user, List<String> permissions) {
        return userRepo.getTenantPermissionsFor(user.getId()).stream()
                .filter(isTenantOrSystemOrSubtenantScope)
                .map(TenantPermission::permissionName)
                .filter(permissions::contains)
                .toList();
    }

    @Introspected
    public record TenantPermission(
            long tenantId,
            String permissionName,
            Permission.PermissionScope permissionScope
    ) {}
}
