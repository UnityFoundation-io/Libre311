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

import app.recaptcha.RecaptchaRequest;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

@Introspected
public class PostRequestServiceRequestDTO implements RecaptchaRequest {
   private static final Logger LOG = LoggerFactory.getLogger(PostRequestServiceRequestDTO.class);


    @NotNull
    @JsonProperty("service_code")
    private Long serviceCode;

    @JsonProperty("lat")
    @NotNull
    @NotBlank
    private String latitude;

    @NotNull
    @NotBlank
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
    @Pattern(regexp = "^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð\\-'. ]+$")
    private String firstName;

    @JsonProperty("last_name")
    @Pattern(regexp = "^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð\\-'. ]+$")
    private String lastName;

    private String phone;

    @Size(max = 4000)
    private String description;

    @JsonProperty("media_url")
    private String mediaUrl;

    @Nullable
    @JsonProperty("client_request_id")
    private String clientRequestId;

    @NotBlank
    @JsonProperty("g_recaptcha_response")
    private String gRecaptchaResponse;

    @JsonIgnore
    private Map<String, String> attributes = new HashMap<>();
    private static final java.util.regex.Pattern ATTRIBUTE_PATTERN = java.util.regex.Pattern.compile("attribute\\[([^]]+)]");
    public PostRequestServiceRequestDTO(Long serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Long getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(Long serviceCode) {
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

    public String getgRecaptchaResponse() {
        return gRecaptchaResponse;
    }

    public void setgRecaptchaResponse(String gRecaptchaResponse) {
        this.gRecaptchaResponse = gRecaptchaResponse;
    }

    @Nullable
    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(@Nullable String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    @JsonAnySetter
    public void processDynamicField(String key, Object value) {
        if (value instanceof Map<?, ?> valueMap) { // id = {attribute: value}
            Object innerValue = valueMap.get("attribute");

            if (innerValue != null) {
                attributes.put("attribute[" + key + "]", innerValue.toString());
            }
        }
        else if (value != null) { // attribute[id] = value
            Matcher matcher = ATTRIBUTE_PATTERN.matcher(key);
            LOG.debug("Processing dynamic field: {}, value = {}", key, value);
            if (matcher.matches()) {
                LOG.debug("Found attribute key: {}", key);
                if (attributes == null) attributes = new HashMap<>();
                String index = matcher.group(1);
                attributes.put("attribute[" + index + "]", value.toString());
            }
        }
    }

    @Override
    public String toString() {
        return "PostRequestServiceRequestDTO{" +
                "serviceCode=" + serviceCode +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", addressString='" + addressString + '\'' +
                ", addressId='" + addressId + '\'' +
                ", email='" + email + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", description='" + description + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", clientRequestId='" + clientRequestId + '\'' +
                ", gRecaptchaResponse='" + gRecaptchaResponse + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
