package app.dto.servicerequest;

import app.model.servicerequest.ServiceRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import java.time.Instant;

@Introspected
public class ServiceRequestRemovalSuggestionDTO {

    private Long id;
    @JsonProperty("service_request_id")
    private Long serviceRequestId;
    private String email;
    @Nullable
    private String name;
    @Nullable
    private String phone;
    private String reason;
    @JsonProperty("date_created")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant dateCreated;

    public ServiceRequestRemovalSuggestionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Long serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }
}
