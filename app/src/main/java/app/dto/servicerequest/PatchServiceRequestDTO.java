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

package app.dto.servicerequest;

import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.QueryValue;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Introspected
public class PatchServiceRequestDTO {

    @Nullable
    @QueryValue(value = "jurisdiction_id")
    private String jurisdictionId;

    private ServiceRequestStatus status;

    private ServiceRequestPriority priority;

    @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    @JsonProperty("agency_email")
    private String agencyEmail;

    @JsonProperty("service_notice")
    private String serviceNotice;

    @Size(max = 4000)
    @JsonProperty("status_notes")
    private String statusNotes;

    @JsonProperty("agency_responsible")
    private String agencyResponsible;

    @JsonProperty("expected_date")
    private Instant expectedDate;

    @JsonProperty("closed_date")
    private Instant closedDate;


    public PatchServiceRequestDTO() {}

    @Nullable
    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(@Nullable String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceRequestStatus status) {
        this.status = status;
    }

    public ServiceRequestPriority getPriority() {
        return priority;
    }

    public void setPriority(ServiceRequestPriority priority) {
        this.priority = priority;
    }

    public String getAgencyEmail() {
        return agencyEmail;
    }

    public void setAgencyEmail(String agencyEmail) {
        this.agencyEmail = agencyEmail;
    }

    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }

    public String getStatusNotes() {
        return statusNotes;
    }

    public void setStatusNotes(String statusNotes) {
        this.statusNotes = statusNotes;
    }

    public String getAgencyResponsible() {
        return agencyResponsible;
    }

    public void setAgencyResponsible(String agencyResponsible) {
        this.agencyResponsible = agencyResponsible;
    }

    public Instant getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(Instant expectedDate) {
        this.expectedDate = expectedDate;
    }

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    // see https://github.com/micronaut-projects/micronaut-core/issues/1853
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
//        m.put("service_code", getServiceCode());
        return m;
    }
}
