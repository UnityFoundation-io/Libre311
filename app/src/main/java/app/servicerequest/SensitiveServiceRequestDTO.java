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
import io.micronaut.core.annotation.Introspected;

import java.util.HashMap;
import java.util.Map;

@Introspected
public class SensitiveServiceRequestDTO extends ServiceRequestDTO {

    @JsonProperty("status_notes")
    private String statusNotes;

    @JsonProperty("agency_email")
    private String agencyEmail;

    private ServiceRequestPriority priority;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("account_id")
    private String accountId;

    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String phone;

    public SensitiveServiceRequestDTO() {
    }

    public SensitiveServiceRequestDTO(ServiceRequest serviceRequest) {
        super(serviceRequest);
        this.statusNotes = serviceRequest.getStatusNotes();
        this.agencyEmail = serviceRequest.getAgencyEmail();
        this.priority = serviceRequest.getPriority();
        this.deviceId = serviceRequest.getDeviceId();
        this.accountId = serviceRequest.getAccountId();
        this.email = serviceRequest.getEmail();
        this.firstName = serviceRequest.getFirstName();
        this.lastName = serviceRequest.getLastName();
        this.phone = serviceRequest.getPhone();
    }

    public String getStatusNotes() {
        return statusNotes;
    }

    public void setStatusNotes(String statusNotes) {
        this.statusNotes = statusNotes;
    }

    public String getAgencyEmail() {
        return agencyEmail;
    }

    public void setAgencyEmail(String agencyEmail) {
        this.agencyEmail = agencyEmail;
    }

    public ServiceRequestPriority getPriority() {
        return priority;
    }

    public void setPriority(ServiceRequestPriority priority) {
        this.priority = priority;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // see https://github.com/micronaut-projects/micronaut-core/issues/1853
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("service_code", getServiceCode());
        return m;
    }
}