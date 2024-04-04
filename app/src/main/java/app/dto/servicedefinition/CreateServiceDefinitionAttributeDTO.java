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

package app.dto.servicedefinition;

import app.model.service.AttributeDataType;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
public class CreateServiceDefinitionAttributeDTO extends UpdateServiceDefinitionAttributeDTO {
    public CreateServiceDefinitionAttributeDTO() {
    }

    public CreateServiceDefinitionAttributeDTO(String code, boolean variable, AttributeDataType datatype, boolean required, String description, int attributeOrder, String datatypeDescription) {
        super(code, variable, datatype, required, description, attributeOrder, datatypeDescription);
    }

    @Override
    public String getCode() {
        return super.getCode();
    }

    @Override
    public void setCode(String code) {
        super.setCode(code);
    }

    @NotNull
    @Override
    public AttributeDataType getDatatype() {
        return super.getDatatype();
    }

    @NotNull
    @Override
    public void setDatatype(AttributeDataType datatype) {
        super.setDatatype(datatype);
    }


    @NotNull
    @Override
    public Boolean isVariable() {
        return super.isVariable();
    }

    @NotNull
    @Override
    public void setVariable(Boolean variable) {
        super.setVariable(variable);
    }

    @NotNull
    @Override
    public Boolean isRequired() {
        return super.isRequired();
    }

    @NotNull
    @Override
    public void setRequired(Boolean required) {
        super.setRequired(required);
    }

    @NotNull
    @Override
    public Integer getAttributeOrder() {
        return super.getAttributeOrder();
    }

    @NotNull
    @Override
    public void setAttributeOrder(Integer attributeOrder) {
        super.setAttributeOrder(attributeOrder);
    }
}
