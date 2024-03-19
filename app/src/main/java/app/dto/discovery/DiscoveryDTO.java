// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package app.dto.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@JsonRootName("discovery")
public class DiscoveryDTO {

    private String changeset;
    private String contact;

    @JsonProperty("key_service")
    private String keyService;

    @JacksonXmlElementWrapper(localName = "endpoints")
    @JacksonXmlProperty(localName = "endpoint")
    private List<DiscoveryEndpointDTO> endpoints;

    public DiscoveryDTO() {
    }

    public String getChangeset() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getKeyService() {
        return keyService;
    }

    public void setKeyService(String keyService) {
        this.keyService = keyService;
    }

    public List<DiscoveryEndpointDTO> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<DiscoveryEndpointDTO> endpoints) {
        this.endpoints = endpoints;
    }
}
