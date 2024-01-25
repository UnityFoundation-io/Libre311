package app.security;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.core.value.OptionalValues;
import jakarta.inject.Singleton;

import java.util.*;
import java.util.function.Predicate;

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

        Collection<String> declaredPermissions = context.getValues(RequiresPermissions.class, String.class).values();

        if (!unityAuthService.isUserPermittedForAction(token, jurisdictionId, declaredPermissions)) {
            throw new RuntimeException("Not Authorized.");
        }

        return context.proceed();
    }
}
