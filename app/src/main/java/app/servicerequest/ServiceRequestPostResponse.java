// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.servicerequest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceRequestPostResponse implements ServiceRequestResponseDTO {

    @JsonProperty("service_request_id")
    private Long id;

    private String token;

    // Information about the action expected to fulfill the request or otherwise address the information reported.
    @JsonProperty("service_notice")
    private String serviceNotice;

    @JsonProperty("account_id")
    private String accountId;

    public ServiceRequestPostResponse() {
    }

    public ServiceRequestPostResponse(ServiceRequest serviceRequest) {
        this.id = serviceRequest.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
