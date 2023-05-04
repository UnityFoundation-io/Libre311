package app.dto.servicerequest;

import app.model.servicerequest.ServiceRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostResponseServiceRequestDTO implements ServiceRequestResponseDTO {

    @JsonProperty("service_request_id")
    private String id;

    private String token;

    // Information about the action expected to fulfill the request or otherwise address the information reported.
    @JsonProperty("service_notice")
    private String serviceNotice;

    @JsonProperty("account_id")
    private String accountId;

    public PostResponseServiceRequestDTO() {
    }

    public PostResponseServiceRequestDTO(ServiceRequest serviceRequest) {
        this.id = serviceRequest.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
