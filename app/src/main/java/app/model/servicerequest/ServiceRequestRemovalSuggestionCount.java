package app.model.servicerequest;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class ServiceRequestRemovalSuggestionCount {
    private Long serviceRequestId;
    private Long count;

    public ServiceRequestRemovalSuggestionCount() {}

    public ServiceRequestRemovalSuggestionCount(Long serviceRequestId, Long count) {
        this.serviceRequestId = serviceRequestId;
        this.count = count;
    }

    public Long getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(Long serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
