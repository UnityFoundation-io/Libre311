package io.unityfoundation.auth;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Serdeable
public record HasPermissionRequest(
    @NotNull Long tenantId,
    @NotNull Long serviceId,
    List<String> permissions

) {}
