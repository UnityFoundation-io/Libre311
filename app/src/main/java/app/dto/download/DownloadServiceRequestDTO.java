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
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;

@Serdeable
public class DownloadServiceRequestDTO {

    @CsvBindByName(column = "service_request_id")
    private Long id;

    @CsvBindByName(column = "jurisdiction_id")
    private String jurisdictionId;

    @CsvDate(value = "yyyy-MM-dd'T'HH:mm'Z'")
    @CsvBindByName(column = "requested_datetime")
    private Instant dateCreated;

    @CsvDate(value = "yyyy-MM-dd'T'HH:mm'Z'")
    @CsvBindByName(column = "updated_datetime")
    private Instant dateUpdated;

    @CsvDate(value = "yyyy-MM-dd'T'HH:mm'Z'")
    @CsvBindByName(column = "closed_datetime")
    private Instant closedDate;

    @CsvBindByName(column = "status_description")
    private String statusDescription;

    @CsvBindByName(column = "status_notes")
    private String statusNotes;

    @CsvBindByName(column = "service_name")
    private String serviceName;

    @CsvBindByName
    private String description;

    @CsvBindByName(column = "agency_responsible")
    private String agencyResponsible;

    @CsvBindByName
    private String address;

    @CsvBindByName(column = "lat")
    private String latitude;

    @CsvBindByName(column = "long")
    private String longitude;

    @CsvBindByName(column = "service_subtype")
    private String serviceSubtype;

    @CsvBindByName
    private String email;

    @CsvBindByName(column = "first_name")
    private String firstName;

    @CsvBindByName(column = "last_name")
    private String lastName;

    @CsvBindByName
    private String phone;

    @CsvBindByName(column = "media_url")
    private String mediaUrl;

    public DownloadServiceRequestDTO(ServiceRequest serviceRequest) {
        this.id = serviceRequest.getId();
        this.dateCreated = serviceRequest.getDateCreated();
        this.dateUpdated = serviceRequest.getDateUpdated();
        this.closedDate = serviceRequest.getClosedDate();
        this.statusDescription = sanitize(serviceRequest.getStatus().toString());
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

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
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

    public void setServiceSubtype(String serviceSubtype) {
        this.serviceSubtype = serviceSubtype;
    }

    private String sanitize(String value) {
        String csvInjectionRegex = "^[=@+\\-\t\r].*";

        if (value == null || !value.matches(csvInjectionRegex))
            return value;

        return "'" + value;
    }
}
