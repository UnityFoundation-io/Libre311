package app.model.servicerequest;

import app.model.service.Service;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "services_id", nullable = false)
    private Service service;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String attributesJson;

    // optional

    @Nullable
    private String latitude;

    @Nullable
    private String longitude;

    @Nullable
    private String addressString;

    // The internal address ID used by a jurisdiction’s master address repository or other addressing system.
    @Nullable
    private String addressId;

    @Nullable
    @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    private String email;

    // The unique device ID of the device submitting the request. This is usually only used for mobile devices.
    @Nullable
    private String deviceId;

    // The unique ID for the user account of the person submitting the request
    @Nullable
    private String accountId;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String phone;

    @Nullable
    @Size(max = 4000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;

    @Nullable
    @Column(columnDefinition = "TEXT")
    private String statusNotes;

    @Nullable
    private String agencyResponsible;

    @Nullable
    private String serviceNotice;

    @Nullable
    @Pattern(regexp = "^\\d{5}(?:[-\\s]\\d{4})?$")
    private String zipCode;

    @Nullable
    private Instant expectedDate;

    @Nullable
    private Instant closedDate;

    @DateCreated
    private Instant dateCreated;

    @DateUpdated
    private Instant dateUpdated;

    public ServiceRequest() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Nullable
    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(@Nullable String latitude) {
        this.latitude = latitude;
    }

    @Nullable
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(@Nullable String longitude) {
        this.longitude = longitude;
    }

    @Nullable
    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(@Nullable String addressString) {
        this.addressString = addressString;
    }

    @Nullable
    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(@Nullable String addressId) {
        this.addressId = addressId;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@Nullable String deviceId) {
        this.deviceId = deviceId;
    }

    @Nullable
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(@Nullable String accountId) {
        this.accountId = accountId;
    }

    @Nullable
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nullable String firstName) {
        this.firstName = firstName;
    }

    @Nullable
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@Nullable String lastName) {
        this.lastName = lastName;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(@Nullable String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public ServiceRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceRequestStatus status) {
        this.status = status;
    }

    @Nullable
    public String getStatusNotes() {
        return statusNotes;
    }

    public void setStatusNotes(@Nullable String statusNotes) {
        this.statusNotes = statusNotes;
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

    @Nullable
    public Instant getExpectedDate() {
        return expectedDate;
    }

    @Nullable
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(@Nullable String zipCode) {
        this.zipCode = zipCode;
    }

    @Nullable
    public String getAgencyResponsible() {
        return agencyResponsible;
    }

    public void setAgencyResponsible(@Nullable String agencyResponsible) {
        this.agencyResponsible = agencyResponsible;
    }

    @Nullable
    public String getServiceNotice() {
        return serviceNotice;
    }

    public void setServiceNotice(@Nullable String serviceNotice) {
        this.serviceNotice = serviceNotice;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Nullable
    public String getAttributesJson() {
        return attributesJson;
    }

    public void setAttributesJson(@Nullable String attributesJson) {
        this.attributesJson = attributesJson;
    }

    @Nullable
    public Instant getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(@Nullable Instant closedDate) {
        this.closedDate = closedDate;
    }
}
