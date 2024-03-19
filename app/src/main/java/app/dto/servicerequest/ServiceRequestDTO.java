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

import app.dto.servicedefinition.ServiceDefinitionAttributeDTO;
import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Serdeable
public class ServiceRequestDTO implements ServiceRequestResponseDTO {

    @JsonProperty("service_request_id")
    private Long id;

    @JsonProperty("jurisdiction_id")
    private String jurisdictionId;

    private ServiceRequestStatus status;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("service_code")
    private String serviceCode;

    private String description;

    @JsonProperty("agency_responsible")
    private String agencyResponsible;

    @JsonProperty("service_notice")
    private String serviceNotice;

    @JsonProperty("requested_datetime")
    private Instant dateCreated;

    @JsonProperty("updated_datetime")
    private Instant dateUpdated;

    @JsonProperty("expected_datetime")
    private Instant expectedDate;

    @JsonProperty("closed_datetime")
    private Instant closedDate;

    @JsonProperty("address")
    private String address;

    // The internal address ID used by a jurisdiction’s master address repository or other addressing system.
    @JsonProperty("address_id")
    private String addressId;

    @JsonProperty("zipcode")
    private String zipCode;

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("long")
    private String longitude;

    @JsonProperty("media_url")
    private String mediaUrl;

    @JsonProperty("selected_values")
    private List<ServiceDefinitionAttributeDTO> selectedValues;

    public ServiceRequestDTO() {
    }

    public ServiceRequestDTO(ServiceRequest serviceRequest) {
        this.id = serviceRequest.getId();
        this.status = serviceRequest.getStatus();
        this.serviceName = serviceRequest.getService().getServiceName(); //
        this.serviceCode = serviceRequest.getService().getServiceCode();
        this.description = serviceRequest.getDescription();
        this.agencyResponsible = serviceRequest.getAgencyResponsible();
        this.serviceNotice = serviceRequest.getServiceNotice();
        this.dateCreated = serviceRequest.getDateCreated();
        this.dateUpdated = serviceRequest.getDateUpdated();
        this.expectedDate = serviceRequest.getExpectedDate();
        this.address = serviceRequest.getAddressString();
        this.addressId = serviceRequest.getAddressId();
        this.zipCode = serviceRequest.getZipCode();
        this.latitude = serviceRequest.getLatitude();
        this.longitude = serviceRequest.getLongitude();
        this.mediaUrl = serviceRequest.getMediaUrl();
        this.closedDate = serviceRequest.getClosedDate();
        if (serviceRequest.getJurisdiction() != null) {
            this.jurisdictionId = serviceRequest.getJurisdiction().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceRequestStatus status) {
        this.status = status;
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

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getAgencyResponsible() {
        return agencyResponsible;
    }

    public void setAgencyResponsible(String agencyResponsible) {
        this.agencyResponsible = agencyResponsible;
    }

    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Instant getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Instant dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Instant getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(Instant expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    // see https://github.com/micronaut-projects/micronaut-core/issues/1853
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("service_code", getServiceCode());
        return m;
    }

    public List<ServiceDefinitionAttributeDTO> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<ServiceDefinitionAttributeDTO> selectedValues) {
        this.selectedValues = selectedValues;
    }
}
