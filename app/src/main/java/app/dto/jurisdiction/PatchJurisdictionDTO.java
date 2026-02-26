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

package app.dto.jurisdiction;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Introspected
public class PatchJurisdictionDTO {

    @NotEmpty
    private String name;

    @JsonProperty("abbreviated_name")
    @Size(max = 8)
    private String abbreviatedName;

    @JsonProperty("primary_color")
    @Pattern(regexp = "^\\d{1,3}\\s\\d{1,3}%\\s\\d{1,3}%$",
             message = "Must be valid HSL format: hue saturation% lightness%")
    private String primaryColor;

    @JsonProperty("primary_hover_color")
    @Pattern(regexp = "^\\d{1,3}\\s\\d{1,3}%\\s\\d{1,3}%$",
             message = "Must be valid HSL format: hue saturation% lightness%")
    private String primaryHoverColor;

    @JsonProperty("logo_media_url")
    private String logoMediaUrl;

    @Size(min = 4)
    private Double[][] bounds;

    @JsonProperty("terms_of_use_content")
    @Size(max = 50000, message = "Terms of use content must not exceed 50000 characters")
    private String termsOfUseContent;

    @JsonProperty("privacy_policy_content")
    @Size(max = 50000, message = "Privacy policy content must not exceed 50000 characters")
    private String privacyPolicyContent;

    @JsonProperty("project_feature")
    private app.model.jurisdiction.ProjectFeature projectFeature;

    @JsonProperty("closed_request_days_visible_user")
    private Integer closedRequestDaysVisibleUser;

    @JsonProperty("closed_request_days_visible_admin")
    private Integer closedRequestDaysVisibleAdmin;

    public PatchJurisdictionDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviatedName() {
        return abbreviatedName;
    }

    public void setAbbreviatedName(String abbreviatedName) {
        this.abbreviatedName = abbreviatedName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getPrimaryHoverColor() {
        return primaryHoverColor;
    }

    public void setPrimaryHoverColor(String primaryHoverColor) {
        this.primaryHoverColor = primaryHoverColor;
    }

    public String getLogoMediaUrl() {
        return logoMediaUrl;
    }

    public void setLogoMediaUrl(String logoMediaUrl) {
        this.logoMediaUrl = logoMediaUrl;
    }

    public Double[][] getBounds() {
        return bounds;
    }

    public void setBounds(Double[][] bounds) {
        this.bounds = bounds;
    }

    public String getTermsOfUseContent() {
        return termsOfUseContent;
    }

    public void setTermsOfUseContent(String termsOfUseContent) {
        this.termsOfUseContent = termsOfUseContent;
    }

    public String getPrivacyPolicyContent() {
        return privacyPolicyContent;
    }

    public void setPrivacyPolicyContent(String privacyPolicyContent) {
        this.privacyPolicyContent = privacyPolicyContent;
    }

    public app.model.jurisdiction.ProjectFeature getProjectFeature() {
        return projectFeature;
    }

    public void setProjectFeature(app.model.jurisdiction.ProjectFeature projectFeature) {
        this.projectFeature = projectFeature;
    }

    public Integer getClosedRequestDaysVisibleUser() {
        return closedRequestDaysVisibleUser;
    }

    public void setClosedRequestDaysVisibleUser(Integer closedRequestDaysVisibleUser) {
        this.closedRequestDaysVisibleUser = closedRequestDaysVisibleUser;
    }

    public Integer getClosedRequestDaysVisibleAdmin() {
        return closedRequestDaysVisibleAdmin;
    }

    public void setClosedRequestDaysVisibleAdmin(Integer closedRequestDaysVisibleAdmin) {
        this.closedRequestDaysVisibleAdmin = closedRequestDaysVisibleAdmin;
    }
}
