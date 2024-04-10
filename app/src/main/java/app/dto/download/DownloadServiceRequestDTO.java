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

import app.model.servicerequest.ServiceRequest;
import app.model.servicerequest.ServiceRequestPriority;
import app.model.servicerequest.ServiceRequestStatus;
import io.micronaut.core.annotation.Introspected;

import java.time.Instant;

@Introspected
public class DownloadServiceRequestDTO {

    private String jurisdictionId;
    private String serviceName;
    private String group;
    private Long serviceCode;
    private Long id;
    private String serviceSubtype;
    private String description;
    private String mediaUrl;
    private String address;
    private String zipcode;
    private String latitude;
    private String longitude;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Instant dateCreated;
    private Instant dateUpdated;
    private Instant closedDate;
    private String agencyResponsible;
    private String agencyEmail;
    private ServiceRequestPriority priority;
    private ServiceRequestStatus status;
    private String statusNotes;
    private String serviceNotice;

    public DownloadServiceRequestDTO(ServiceRequest serviceRequest) {
        this.id = serviceRequest.getId();
        this.dateCreated = serviceRequest.getDateCreated();
        this.dateUpdated = serviceRequest.getDateUpdated();
        this.closedDate = serviceRequest.getClosedDate();
        this.status = serviceRequest.getStatus();
        this.statusNotes = sanitize(serviceRequest.getStatusNotes());
        this.serviceName = sanitize(serviceRequest.getService().getServiceName());
        this.description = sanitize(serviceRequest.getDescription());
        this.agencyResponsible = sanitize(serviceRequest.getAgencyResponsible());
        this.address = sanitize(serviceRequest.getAddressString());
        this.latitude = sanitize(serviceRequest.getLatitude());
        this.longitude = sanitize(serviceRequest.getLongitude());
        this.email = sanitize(serviceRequest.getEmail());
        this.firstName = sanitize(serviceRequest.getFirstName());
        this.lastName = sanitize(serviceRequest.getLastName());
        this.phone = sanitize(serviceRequest.getPhone());
        this.mediaUrl = sanitize(serviceRequest.getMediaUrl());
        this.serviceNotice = sanitize(serviceRequest.getServiceNotice());
        this.serviceCode = serviceRequest.getService().getId();
        this.zipcode = sanitize(serviceRequest.getZipCode());
        this.agencyEmail = sanitize(serviceRequest.getAgencyEmail());
        this.priority = serviceRequest.getPriority();
        if (serviceRequest.getService().getServiceGroup() != null) {
            this.group = sanitize(serviceRequest.getService().getServiceGroup().getName());
        }
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

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public String getStatusNotes() {
        return statusNotes;
    }

    public void setStatusNotes(String statusNotes) {
        this.statusNotes = statusNotes;
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

    public String getAgencyResponsible() {
        return agencyResponsible;
    }

    public void setAgencyResponsible(String agencyResponsible) {
        this.agencyResponsible = agencyResponsible;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceRequestStatus status) {
        this.status = status;
    }

    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Instant closedDate) {
        this.closedDate = closedDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getServiceSubtype() {
        return serviceSubtype;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(Long serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
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

    public void setServiceSubtype(String serviceSubtype) {
        this.serviceSubtype = serviceSubtype;
    }

    private String sanitize(String value) {
        String csvInjectionRegex = "^[=@+\\-\t\r].*";

        if (value == null || !value.matches(csvInjectionRegex))
            return value;

        return "'" + value;
    }

    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }
}
