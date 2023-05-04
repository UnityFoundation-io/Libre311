package app.dto.download;

import app.model.servicerequest.ServiceRequest;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import io.micronaut.core.annotation.Introspected;

import java.time.Instant;

@Introspected
public class DownloadServiceRequestDTO {

    @CsvBindByName(column = "service_request_id")
    private Long id;

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
        this.statusDescription = serviceRequest.getStatus().toString();
        this.statusNotes = serviceRequest.getStatusNotes();
        this.serviceName = serviceRequest.getService().getServiceName();
        this.description = serviceRequest.getDescription();
        this.agencyResponsible = serviceRequest.getAgencyResponsible();
        this.address = serviceRequest.getAddressString();
        this.latitude = serviceRequest.getLatitude();
        this.longitude = serviceRequest.getLongitude();
        this.email = serviceRequest.getEmail();
        this.firstName = serviceRequest.getFirstName();
        this.lastName = serviceRequest.getLastName();
        this.phone = serviceRequest.getPhone();
        this.mediaUrl = serviceRequest.getMediaUrl();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
