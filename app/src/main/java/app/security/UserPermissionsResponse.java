package app.security;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.micronaut.core.annotation.Introspected;
import java.util.List;

@JsonTypeInfo(use = Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserPermissionsResponse.Success.class),
    @JsonSubTypes.Type(value = UserPermissionsResponse.Failure.class)
})
public interface UserPermissionsResponse {
    @Introspected
    class Success implements UserPermissionsResponse {
        List<String> permissions;
        public List<String> getPermissions() {
            return permissions;
        }
        public void setPermissions(List<String> permissions) {
            this.permissions = permissions;
        }
    }
    @Introspected
    class Failure implements UserPermissionsResponse {
        String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
