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

import io.micronaut.core.annotation.Introspected;

import jakarta.validation.constraints.Min;

@Introspected
public class PatchAttributeOrderDTO {

    private Long code;

    @Min(0)
    private Integer order;

    public PatchAttributeOrderDTO() {
    }

    public PatchAttributeOrderDTO(Long code, Integer order) {
        this.code = code;
        this.order = order;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
