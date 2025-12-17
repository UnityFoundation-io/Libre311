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

package app.servicerequest;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "service_requests")
public class ServiceRequestList {

    @JsonProperty("request")
    private List<? extends ServiceRequestResponseDTO> serviceRequests;

    public ServiceRequestList(List<? extends ServiceRequestResponseDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }

    public List<? extends ServiceRequestResponseDTO> getServiceRequests() {
        return serviceRequests;
    }

    public void setServiceRequests(List<? extends ServiceRequestResponseDTO> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }
}
