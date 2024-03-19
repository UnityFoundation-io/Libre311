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

package app.dto.service;

import app.model.service.Service;
import app.model.service.ServiceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ServiceDTO {

    private Long id;

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("jurisdiction_id")
    private String jurisdictionId;

    @JsonProperty("service_name")
    private String serviceName;

    private String description;

    private boolean metadata;

    private ServiceType type;

    @JsonProperty("group_id")
    private Long groupId;

    public ServiceDTO() {
    }

    public ServiceDTO(Service service, boolean hasMetadata) {
        this.id = service.getId();
        this.serviceCode = service.getServiceCode();
        this.serviceName = service.getServiceName();
        this.description = service.getDescription();
        this.metadata = hasMetadata;
        this.groupId = service.getServiceGroup().getId();
        this.type = service.getType();
        if (service.getJurisdiction() != null) {
            this.jurisdictionId = service.getJurisdiction().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
