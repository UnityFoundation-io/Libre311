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

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionBoundary;
import app.service.geometry.LibreGeometryFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.micronaut.core.annotation.Introspected;
import org.locationtech.jts.geom.Polygon;

import java.util.Set;

@Introspected
public class JurisdictionDTO {

    private String name;

    @JsonProperty("abbreviated_name")
    private String abbreviatedName;

    @JsonProperty("jurisdiction_id")
    private String jurisdictionId;

    @JsonProperty("tenant_id")
    private Long tenantId;

    // site settings
    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("primary_hover_color")
    private String primaryHoverColor;

    @JsonProperty("logo_media_url")
    private String logoMediaUrl;

    @JsonProperty("auth_base_url")
    private String unityAuthUrl;

    @JsonProperty("terms_of_use_content")
    private String termsOfUseContent;

    @JsonProperty("privacy_policy_content")
    private String privacyPolicyContent;

    @JsonProperty("remote_hosts")
    private Set<String> remoteHosts;

    @JsonProperty("project_feature")
    private app.model.jurisdiction.ProjectFeature projectFeature;

    @JsonProperty("closed_request_days_visible_user")
    private Integer closedRequestDaysVisibleUser;

    @JsonProperty("closed_request_days_visible_admin")
    private Integer closedRequestDaysVisibleAdmin;

    private Double[][] bounds;

    public JurisdictionDTO() {
    }

    public JurisdictionDTO(Jurisdiction jurisdiction, String unityAuthUrl) {
        this(jurisdiction);
        this.unityAuthUrl = unityAuthUrl;
    }

    public JurisdictionDTO(Jurisdiction jurisdiction) {
        this.jurisdictionId = jurisdiction.getId();
        this.name = jurisdiction.getName();
        this.abbreviatedName = jurisdiction.getAbbreviatedName();
        this.tenantId = jurisdiction.getTenantId();
        this.primaryColor = jurisdiction.getPrimaryColor();
        this.primaryHoverColor = jurisdiction.getPrimaryHoverColor();
        this.logoMediaUrl = jurisdiction.getLogoMediaUrl();
        this.termsOfUseContent = jurisdiction.getTermsOfUseContent();
        this.privacyPolicyContent = jurisdiction.getPrivacyPolicyContent();
        this.projectFeature = jurisdiction.getProjectFeature();
        this.closedRequestDaysVisibleUser = jurisdiction.getClosedRequestDaysVisibleUser();
        this.closedRequestDaysVisibleAdmin = jurisdiction.getClosedRequestDaysVisibleAdmin();
    }

    public JurisdictionDTO(Jurisdiction jurisdiction, JurisdictionBoundary boundary) {
        this(jurisdiction);
        setBounds(boundary.getBoundary());
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public String getUnityAuthUrl() {
        return unityAuthUrl;
    }

    public void setUnityAuthUrl(String unityAuthUrl) {
        this.unityAuthUrl = unityAuthUrl;
    }

    public Double[][] getBounds() {
        return bounds;
    }

    public void setBounds(Polygon polygon) {
        this.bounds = LibreGeometryFactory.getCoordinatesFrom(polygon);
    }

    @JsonSetter
    public void setBounds(Double[][] bounds) {
        this.bounds = bounds;
    }

    public Set<String> getRemoteHosts() {
        return remoteHosts;
    }

    public void setRemoteHosts(Set<String> remoteHosts) {
        this.remoteHosts = remoteHosts;
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
