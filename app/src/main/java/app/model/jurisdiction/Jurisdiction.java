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
import app.model.tenant.Tenant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jurisdictions")
public class Jurisdiction {

    @Id
    private String id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<RemoteHost> remoteHosts = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Service> services = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "jurisdiction")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ServiceRequest> serviceRequests = new HashSet<>();

    public Jurisdiction(String id, Tenant tenant) {
        this.id = id;
        this.tenant = tenant;
    }

    public Jurisdiction(String id, String name, RemoteHost remoteHost) {
        this.id = id;
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

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
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

}