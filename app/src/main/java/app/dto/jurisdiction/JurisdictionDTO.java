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

@Introspected
public class JurisdictionDTO {

    private String name;

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
        this.tenantId = jurisdiction.getTenantId();
        this.primaryColor = jurisdiction.getPrimaryColor();
        this.primaryHoverColor = jurisdiction.getPrimaryHoverColor();
        this.logoMediaUrl = jurisdiction.getLogoMediaUrl();
    }

    public JurisdictionDTO(Jurisdiction jurisdiction, JurisdictionBoundary boundary) {
        this.jurisdictionId = jurisdiction.getId();
        this.name = jurisdiction.getName();
        this.tenantId = jurisdiction.getTenantId();
        this.primaryColor = jurisdiction.getPrimaryColor();
        this.primaryHoverColor = jurisdiction.getPrimaryHoverColor();
        this.logoMediaUrl = jurisdiction.getLogoMediaUrl();
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
}
