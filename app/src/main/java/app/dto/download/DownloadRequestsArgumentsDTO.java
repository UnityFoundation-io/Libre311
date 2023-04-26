package app.dto.download;

import app.model.servicerequest.ServiceRequestStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.QueryValue;

import java.time.Instant;

@Introspected
public class DownloadRequestsArgumentsDTO {

    @Nullable
    @QueryValue(value = "service_name")
    private String serviceName;

    @Nullable
    @QueryValue(value = "start_date")
    private Instant startDate;

    @Nullable
    @QueryValue(value = "end_date")
    private Instant endDate;

    @Nullable
    @QueryValue
    private ServiceRequestStatus status;


    public DownloadRequestsArgumentsDTO() {
    }

    @Nullable
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(@Nullable String serviceName) {
        this.serviceName = serviceName;
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
}
