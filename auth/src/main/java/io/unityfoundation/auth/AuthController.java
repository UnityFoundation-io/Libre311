package io.unityfoundation.auth;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.serde.annotation.Serdeable;
import io.unityfoundation.auth.entities.*;
import io.unityfoundation.auth.entities.Service.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Secured("USER")
@Controller
public class AuthController {

  private final UserRepo userRepo;
  private final ServiceRepo serviceRepo;
  private final TenantRepo tenantRepo;
  private final RoleRepo roleRepo;
  private final PermissionsService permissionsService;

  public AuthController(UserRepo userRepo, ServiceRepo serviceRepo, TenantRepo tenantRepo, RoleRepo roleRepo, PermissionsService permissionsService) {
    this.userRepo = userRepo;
    this.serviceRepo = serviceRepo;
    this.tenantRepo = tenantRepo;
      this.roleRepo = roleRepo;
      this.permissionsService = permissionsService;
  }

  @Post("/principal/permissions")
  public UserPermissionsResponse permissions(@Body UserPermissionsRequest requestDTO,
      Authentication authentication) {
    Optional<Tenant> maybeTenant = tenantRepo.findById(requestDTO.tenantId());
    if (maybeTenant.isEmpty()){
      return new UserPermissionsResponse.Failure("No tenant found.");
    }
    Tenant tenant = maybeTenant.get();

    if (!tenant.getStatus().equals(Tenant.TenantStatus.ENABLED)){
      return new UserPermissionsResponse.Failure("The tenant is not enabled.");
    }

    User user = userRepo.findByEmail(authentication.getName()).orElse(null);
    if (checkUserStatus(user)) {
      return new UserPermissionsResponse.Failure("The user's account has been disabled.");
    }

    Service service = serviceRepo.findById(requestDTO.serviceId())
        .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Service not found."));

    if (service.getStatus() == ServiceStatus.DISABLED) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The service is disabled.");
    } else if (service.getStatus() == ServiceStatus.DOWN_FOR_MAINTENANCE) {

      throw new HttpStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "The service is down for maintenance.");
    }

    if (!userRepo.isServiceAvailable(user.getId(), service.getId())) {
      return new UserPermissionsResponse.Failure(
          "The Tenant and/or Service is not available for this user");
    }

    return new UserPermissionsResponse.Success(permissionsService.getPermissionsFor(user, tenant));
  }

  @Post("/hasPermission")
  public HttpResponse<HasPermissionResponse> hasPermission(@Body HasPermissionRequest requestDTO,
      Authentication authentication) {

    Optional<Tenant> tenantOptional = tenantRepo.findById(requestDTO.tenantId());
    if (tenantOptional.isEmpty()) {
      return createHasPermissionResponse(false, authentication.getName(),"Cannot find tenant!", List.of());
    }

    User user = userRepo.findByEmail(authentication.getName()).orElse(null);
    if (checkUserStatus(user)) {
      return createHasPermissionResponse(false, authentication.getName(), "The user’s account has been disabled!", List.of());
    }

    Optional<Service> service = serviceRepo.findById(requestDTO.serviceId());

    String serviceStatusCheckResult = checkServiceStatus(service);
    if (serviceStatusCheckResult != null) {
      return createHasPermissionResponse(false, user.getEmail(), serviceStatusCheckResult, List.of());
    }

    if (!userRepo.isServiceAvailable(user.getId(), service.get().getId())) {
      return createHasPermissionResponse(false, user.getEmail(), "The requested service is not enabled for the requested tenant!", List.of());
    }

    List<String> commonPermissions = permissionsService.checkUserPermission(user, tenantOptional.get(), requestDTO.permissions());
    if (commonPermissions.isEmpty()) {
      return createHasPermissionResponse(false, user.getEmail(), "The user does not have permission!", commonPermissions);
    }

    return createHasPermissionResponse(true, user.getEmail(), null, commonPermissions);
  }

  @Get("/roles")
  public HttpResponse<List<RoleDTO>> getRoles(Authentication authentication) {

    User user = userRepo.findByEmail(authentication.getName()).orElse(null);
    if (checkUserStatus(user)) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user is disabled.");
    }

    List<String> commonPermissions = permissionsService.checkUserPermissionsAcrossAllTenants(
            user, List.of("AUTH_SERVICE_VIEW-SYSTEM", "AUTH_SERVICE_VIEW-TENANT"));
    if (commonPermissions.isEmpty()) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user does not have permission!");
    }

    return HttpResponse.ok(roleRepo.findAll().stream()
            .map(role -> new RoleDTO(role.getId(), role.getName(), role.getDescription()))
            .toList());
  }

  @Get("/tenants")
  public HttpResponse<List<TenantDTO>> getTenants(Authentication authentication) {

    String authenticatedUserEmail = authentication.getName();
    User user = userRepo.findByEmail(authenticatedUserEmail).orElse(null);
    if (checkUserStatus(user)) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user is disabled.");
    }

    List<String> commonPermissions = permissionsService.checkUserPermissionsAcrossAllTenants(
            user, List.of("AUTH_SERVICE_VIEW-SYSTEM", "AUTH_SERVICE_VIEW-TENANT"));
    if (commonPermissions.isEmpty()) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user does not have permission!");
    }

    List<Tenant> tenants = userRepo.existsByEmailAndRoleEqualsUnityAdmin(authenticatedUserEmail) ?
            tenantRepo.findAll() : tenantRepo.findAllByUserEmail(authenticatedUserEmail);

    return HttpResponse.ok(tenants.stream()
            .map(tenant -> new TenantDTO(tenant.getId(), tenant.getName()))
            .toList());
  }

  @Get("/tenants/{id}/users")
  public HttpResponse<List<UserResponse>> getTenantUsers(@PathVariable Long id, Authentication authentication) {

    // reject if the declared tenant does not exist
    Optional<Tenant> tenantOptional = tenantRepo.findById(id);
    if (tenantOptional.isEmpty()) {
      throw new HttpStatusException(HttpStatus.NOT_FOUND, "Tenant not found.");
    }

    User user = userRepo.findByEmail(authentication.getName()).orElse(null);
    if (checkUserStatus(user)) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user is disabled.");
    }

    List<String> commonPermissions = permissionsService.checkUserPermission(user, tenantOptional.get(),
            List.of("AUTH_SERVICE_VIEW-SYSTEM", "AUTH_SERVICE_VIEW-TENANT"));
    if (commonPermissions.isEmpty()) {
      throw new HttpStatusException(HttpStatus.FORBIDDEN, "The user does not have permission!");
    }

    // todo: it would be nice to capture the roles and have them automatically mapped to UserResponse.roles
    List<UserResponse> tenantUsers = userRepo.findAllByTenantId(id).stream().map(tenantUser ->
            new UserResponse(tenantUser.getId(), tenantUser.getEmail(), tenantUser.getFirstName(), tenantUser.getLastName(),
                    userRepo.getUserRolesByUserId(tenantUser.getId())
                    )).toList();

    return HttpResponse.ok(tenantUsers);
  }

  private boolean checkUserStatus(User user) {
    return user == null || user.getStatus() != User.UserStatus.ENABLED;
  }

  private String checkServiceStatus(Optional<Service> service) {
    if (service.isEmpty()) {
      return "The service does not exists!";
    } else {
      ServiceStatus status = service.get().getStatus();
      if (ServiceStatus.DISABLED.equals(status)) {
        return "The service is disabled!";
      } else if (ServiceStatus.DOWN_FOR_MAINTENANCE.equals(status)) {
        return "The service is temporarily down for maintenance!";
      }
    }
    return null;
  }

  private HttpResponse<HasPermissionResponse> createHasPermissionResponse(boolean hasPermission,
                                                                          String userEmail,
                                                                          String message,
                                                                          List<String> permissions) {
    return HttpResponse.ok(new HasPermissionResponse(hasPermission, userEmail, message, permissions));
  }

  @Serdeable
  public record TenantDTO(
      Long id,
      String name
  ) {}

  @Serdeable
  public record RoleDTO(
      Long id,
      String name,
      String description
  ) {}

  @Serdeable
  public record HasPermissionResponse(
      boolean hasPermission,
      @Nullable String userEmail,
      @Nullable String errorMessage,
      List<String> permissions
  ) {}

  public sealed interface UserPermissionsResponse {
    @Serdeable
    record Success(List<String> permissions) implements UserPermissionsResponse {}
    @Serdeable
    record Failure(String errorMessage) implements  UserPermissionsResponse {}
  }

  @Serdeable
  public record UserPermissionsRequest(@NotNull Long tenantId,
                                       @NotNull Long serviceId) {}

}
