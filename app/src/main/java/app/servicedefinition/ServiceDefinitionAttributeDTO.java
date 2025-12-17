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

package app.servicedefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class ServiceDefinitionAttributeDTO {

    @JsonProperty("code")
    private Long id;
    private Boolean variable;
    private AttributeDataType datatype;
    private Boolean required;
    private String description;

    @JsonProperty("order")
    private Integer attributeOrder;

    @JsonProperty("datatype_description")
    private String datatypeDescription;

    @JacksonXmlElementWrapper(localName = "values")
    @JacksonXmlProperty(localName = "value")
    private List<AttributeValueDTO> values;

    public ServiceDefinitionAttributeDTO() {
    }

    public ServiceDefinitionAttributeDTO(Long id, boolean variable, AttributeDataType datatype, boolean required, String description, int attributeOrder, String datatypeDescription) {
        this.id = id;
        this.variable = variable;
        this.datatype = datatype;
        this.required = required;
        this.description = description;
        this.attributeOrder = attributeOrder;
        this.datatypeDescription = datatypeDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isVariable() {
        return variable;
    }

    public void setVariable(Boolean variable) {
        this.variable = variable;
    }

    public AttributeDataType getDatatype() {
        return datatype;
    }

    public void setDatatype(AttributeDataType datatype) {
        this.datatype = datatype;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getDatatypeDescription() {
        return datatypeDescription;
    }

    public void setDatatypeDescription(String datatypeDescription) {
        this.datatypeDescription = datatypeDescription;
    }

    public Integer getAttributeOrder() {
        return attributeOrder;
    }

    public void setAttributeOrder(Integer attributeOrder) {
        this.attributeOrder = attributeOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AttributeValueDTO> getValues() {
        return values;
    }

    public void setValues(List<AttributeValueDTO> values) {
        this.values = values;
    }
}
