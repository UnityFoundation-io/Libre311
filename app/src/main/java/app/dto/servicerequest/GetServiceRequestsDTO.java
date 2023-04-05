package app.dto.servicerequest;

import app.model.servicerequest.ServiceRequestStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.annotation.QueryValue;

import javax.validation.Valid;
import java.time.Instant;

@Introspected
public class GetServiceRequestsDTO {

    @Nullable
    @QueryValue(value = "service_request_id")
    private String id;

    @Nullable
    @QueryValue(value = "service_code")
    private String serviceCode;

    @Nullable
    @QueryValue(value = "start_date")
    private Instant startDate;

    @Nullable
    @QueryValue(value = "end_date")
    private Instant endDate;

    @Nullable
    @QueryValue
    private ServiceRequestStatus status;

    @Valid
    private Pageable pageable;

    public GetServiceRequestsDTO() {
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(@Nullable String serviceCode) {
        this.serviceCode = serviceCode;
    }

    @Nullable
    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(@Nullable Instant startDate) {
        this.startDate = startDate;
    }

    @Nullable
    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(@Nullable Instant endDate) {
        this.endDate = endDate;
    }

    @Nullable
    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(@Nullable ServiceRequestStatus status) {
        this.status = status;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }
}
