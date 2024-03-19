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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class DiscoveryEndpointDTO {

    private String specification;
    private String url;
    private String changeset;
    private String type;

    @JacksonXmlElementWrapper(localName = "formats")
    @JacksonXmlProperty(localName = "format")
    private List<String> formats;

    public DiscoveryEndpointDTO() {
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChangeset() {
        return changeset;
    }

    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }
}
