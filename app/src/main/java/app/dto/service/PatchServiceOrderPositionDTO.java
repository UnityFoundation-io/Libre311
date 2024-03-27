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

package app.dto.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.Min;

@Introspected
public class PatchServiceOrderPositionDTO {

    @JsonProperty("service_id")
    private Long serviceId;

    @Min(0)
    @JsonProperty("order_position")
    private Integer orderPosition;

    public PatchServiceOrderPositionDTO() {
    }

    public PatchServiceOrderPositionDTO(Long serviceId, Integer orderPosition) {
        this.serviceId = serviceId;
        this.orderPosition = orderPosition;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getOrderPosition() {
        return orderPosition;
    }

    public void setOrderPosition(Integer orderPosition) {
        this.orderPosition = orderPosition;
    }
}
