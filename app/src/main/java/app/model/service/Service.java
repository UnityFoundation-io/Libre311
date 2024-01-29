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

package app.model.service;

import app.model.jurisdiction.Jurisdiction;
import app.model.service.group.ServiceGroup;
import app.model.service.keyword.ServiceKeyword;
import app.model.service.servicedefinition.ServiceDefinition;
import app.model.service.servicedefinition.ServiceDefinitionEntity;
import app.model.servicerequest.ServiceRequest;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceCode;

    @ManyToOne
    @JoinColumn(name = "jurisdiction_id")
    private Jurisdiction jurisdiction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String keywords;
    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name="service_service_groups",
        joinColumns={@JoinColumn(name="service_id")},
        inverseJoinColumns={@JoinColumn(name="service_group_id")})
    private Set<ServiceGroup> serviceGroups = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    @JoinTable(name="service_service_keywords",
        joinColumns={@JoinColumn(name="service_id")},
        inverseJoinColumns={@JoinColumn(name="service_keyword_id")})
    private Set<ServiceKeyword> serviceKeywords = new HashSet<>();

    private boolean metadata = false;

    @Enumerated(value = EnumType.STRING)
    private ServiceType type = ServiceType.REALTIME;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "service")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ServiceRequest> serviceRequests = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "service_id")
    @Where(clause = "active = true")
    private List<ServiceDefinitionEntity> serviceDefinitions = new ArrayList<>();


    public Service(String serviceName) {
        this.serviceName = serviceName;
    }

    public Service() {}

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Jurisdiction getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(Jurisdiction jurisdiction) {
        this.jurisdiction = jurisdiction;
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

    public boolean isMetadata() {
        return metadata;
    }

    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public ServiceDefinition getServiceDefinitionJson() {
        if (serviceDefinitions.isEmpty()) return null;
        return serviceDefinitions.get(0).getDefinition();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ServiceRequest> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<ServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public void addServiceRequest(ServiceRequest serviceRequest) {
        serviceRequests.add(serviceRequest);
    }

    public List<ServiceDefinitionEntity> getServiceDefinitions() {
        return serviceDefinitions;
    }

    public void setServiceDefinitions(
        List<ServiceDefinitionEntity> serviceDefinitions) {
        this.serviceDefinitions = serviceDefinitions;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void addServiceGroup(ServiceGroup serviceGroup) {
        if (serviceGroups.isEmpty()) {
            serviceGroups.add(serviceGroup);
        }
    }

    public void setServiceGroups(Set<ServiceGroup> serviceGroups) {
        this.serviceGroups = serviceGroups;
    }

    public Set<ServiceKeyword> getServiceKeywords() {
        return serviceKeywords;
    }

    public void setServiceKeywords(Set<ServiceKeyword> serviceKeywords) {
        this.serviceKeywords = serviceKeywords;
    }
}
