package app.dto.servicerequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Introspected
public class PostRequestServiceRequestDTO {

    @NotBlank
    @JsonProperty("service_code")
    private String serviceCode;

    // todo how to handle this?
    // required only if service definition with required fields
//    private String attribute;

    // optional

    @JsonProperty("lat")
    private String latitude;

    @JsonProperty("long")
    private String longitude;

    @JsonProperty("address_string")
    private String addressString;

    // The internal address ID used by a jurisdiction’s master address repository or other addressing system.
    @JsonProperty("address_id")
    private String addressId;

    @Email(regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    private String email;

    // The unique device ID of the device submitting the request. This is usually only used for mobile devices.
    @JsonProperty("device_id")
    private String deviceId;

    // The unique ID for the user account of the person submitting the request
    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String phone;

    @Size(max = 4000)
    private String description;

    @JsonProperty("media_url")
    private String mediaUrl;

    public PostRequestServiceRequestDTO(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
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

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    // see https://github.com/micronaut-projects/micronaut-core/issues/1853
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("service_code", getServiceCode());
        return m;
    }
}
