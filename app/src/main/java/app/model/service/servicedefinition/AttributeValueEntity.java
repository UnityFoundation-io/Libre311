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

import javax.persistence.*;

@Entity
@Table(name = "service_definition_attribute_values")
public class AttributeValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ServiceDefinitionAttributeEntity serviceDefinitionAttribute;

    private String valueName;

    public AttributeValueEntity() {
    }

    public AttributeValueEntity(ServiceDefinitionAttributeEntity serviceDefinitionAttribute, String valueName) {
        this.serviceDefinitionAttribute = serviceDefinitionAttribute;
        this.valueName = valueName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceDefinitionAttributeEntity getServiceDefinitionAttribute() {
        return serviceDefinitionAttribute;
    }

    public void setServiceDefinitionAttribute(ServiceDefinitionAttributeEntity serviceDefinitionAttribute) {
        this.serviceDefinitionAttribute = serviceDefinitionAttribute;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
