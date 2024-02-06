package app.security;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@InterceptorBean(RequiresPermissions.class)
public class RequiresPermissionsInterceptor implements MethodInterceptor<Object, Object> {

    private final UnityAuthService unityAuthService;

    public RequiresPermissionsInterceptor(UnityAuthService unityAuthService) {
        this.unityAuthService = unityAuthService;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String jurisdictionId = (String) context.getParameters().get("jurisdiction_id").getValue();

        Optional<MutableArgumentValue<?>> bearerToken = context.getParameters().values().stream()
                .filter(mutableArgumentValue -> mutableArgumentValue.getValue().toString().toLowerCase().contains("bearer"))
                .findFirst();
        if (bearerToken.isEmpty()) {
            throw new IllegalArgumentException("Bearer token was not included.");
        }
        String token = bearerToken.get().getValue().toString();

        List<String> declaredPermissions = context.getValues(RequiresPermissions.class, String[].class).values().stream()
                .flatMap((Function<String[], Stream<String>>) strings -> Arrays.stream(strings).map(s -> Permission.valueOf(s).getPermission()))
                .distinct().collect(Collectors.toList());

        if (!unityAuthService.isUserPermittedForAction(token, jurisdictionId, declaredPermissions)) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Not Authorized.");
        }

        return context.proceed();
    }
}
