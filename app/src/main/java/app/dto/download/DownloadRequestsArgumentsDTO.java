// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.dto.download;

import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.List;

@Serdeable
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
    @QueryValue(value = "status")
    private List<ServiceRequestStatus> statuses;

    @Nullable
    @QueryValue(value = "priority")
    private List<ServiceRequestPriority> priorities;


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
    public List<ServiceRequestStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(@Nullable List<ServiceRequestStatus> statuses) {
        this.statuses = statuses;
    }

    @Nullable
    public List<ServiceRequestPriority> getPriorities() {
        return priorities;
    }

    public void setPriorities(@Nullable List<ServiceRequestPriority> priorities) {
        this.priorities = priorities;
    }
}
