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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
@JacksonXmlRootElement(localName = "service_definitions")
public class ServiceDefinitionDTO {

    @JsonProperty("service_code")
    private Long serviceCode;

    @JsonInclude(Include.ALWAYS) // my IDE marks this as the default but without including it null values are not deserialized as empty arrays
    @JacksonXmlElementWrapper(localName = "attributes")
    @JacksonXmlProperty(localName = "attribute")
    private List<ServiceDefinitionAttributeDTO> attributes;

    public ServiceDefinitionDTO() {
    }

    public ServiceDefinitionDTO(Long serviceCode) {
        this.serviceCode = serviceCode;
    }

    public List<ServiceDefinitionAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ServiceDefinitionAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public Long getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(Long serviceCode) {
        this.serviceCode = serviceCode;
    }
}
