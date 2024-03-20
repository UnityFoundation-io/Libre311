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

package app.model.jurisdiction;

import app.model.service.Service;
import app.model.servicerequest.ServiceRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "jurisdictions")
public class Jurisdiction {

    @Id
    private String id;

    private String name;

    @NotNull
    private Long tenantId;

    private String primaryColor;

    private String primaryHoverColor;

    private String logoMediaUrl;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<LatLong> bounds = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<RemoteHost> remoteHosts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Service> services = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ServiceRequest> serviceRequests = new HashSet<>();

    public Jurisdiction(String id, Long tenantId) {
        this.id = id;
        this.tenantId = tenantId;
    }

    public Jurisdiction(String id, Long tenantId, String name, RemoteHost remoteHost) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        remoteHosts.add(remoteHost);
    }

    public Jurisdiction() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<Service> getServices() {
        return services;
    }

    public void setServices(Set<Service> services) {
        this.services = services;
    }

    public Set<ServiceRequest> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(Set<ServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RemoteHost> getRemoteHosts() {
        return remoteHosts;
    }

    public void setRemoteHosts(Set<RemoteHost> remoteHosts) {
        this.remoteHosts = remoteHosts;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public List<LatLong> getBounds() {
        return bounds;
    }

    public void setBounds(List<LatLong> bounds) {
        this.bounds = bounds;
    }
}