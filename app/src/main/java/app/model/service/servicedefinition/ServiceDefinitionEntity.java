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

package app.model.service.servicedefinition;


import app.model.service.Service;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "service_definitions")
public class ServiceDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Service service;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "serviceDefinition")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<ServiceDefinitionAttributeEntity> attributes;


    public ServiceDefinitionEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void addAttribute(ServiceDefinitionAttributeEntity attribute) {
        if (attributes == null) {
            attributes = new HashSet<>();
        }
        attributes.add(attribute);
    }

    public Set<ServiceDefinitionAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ServiceDefinitionAttributeEntity> attributes) {
        this.attributes = attributes;
    }
}