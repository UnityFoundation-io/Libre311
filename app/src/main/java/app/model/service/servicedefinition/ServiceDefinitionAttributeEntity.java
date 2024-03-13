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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "service_definition_attributes")
public class ServiceDefinitionAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ServiceDefinitionEntity serviceDefinition;

    private String code;

    private boolean variable;

    @Enumerated(value = EnumType.STRING)
    private AttributeDataType datatype;

    private boolean required;

    private String description;

    private int attributeOrder;

    private String datatypeDescription;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "serviceDefinitionAttribute")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<AttributeValueEntity> attributeValues;

    public ServiceDefinitionAttributeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceDefinitionEntity getServiceDefinition() {
        return serviceDefinition;
    }

    public void setServiceDefinition(ServiceDefinitionEntity serviceDefinition) {
        this.serviceDefinition = serviceDefinition;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isVariable() {
        return variable;
    }

    public void setVariable(boolean variable) {
        this.variable = variable;
    }

    public AttributeDataType getDatatype() {
        return datatype;
    }

    public void setDatatype(AttributeDataType datatype) {
        this.datatype = datatype;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAttributeOrder() {
        return attributeOrder;
    }

    public void setAttributeOrder(int attributeOrder) {
        this.attributeOrder = attributeOrder;
    }

    public String getDatatypeDescription() {
        return datatypeDescription;
    }

    public void setDatatypeDescription(String datatypeDescription) {
        this.datatypeDescription = datatypeDescription;
    }

    public void addAttributeValue(AttributeValueEntity attributeValue) {
        if (attributeValues == null) {
            attributeValues = new HashSet<>();
        }
        attributeValues.add(attributeValue);
    }

    public Set<AttributeValueEntity> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(Set<AttributeValueEntity> attributeValues) {
        this.attributeValues = attributeValues;
    }
}
