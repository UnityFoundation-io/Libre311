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

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

@Introspected
public class CreateServiceDTO extends UpdateServiceDTO {

    public CreateServiceDTO() {
    }

    @NotBlank
    @Override
    public String getServiceName() {
        return super.getServiceName();
    }

    @NotBlank
    @Override
    public void setServiceName(String serviceName) {
        super.setServiceName(serviceName);
    }

    @NotBlank
    @Override
    public String getServiceCode() {
        return super.getServiceCode();
    }

    @NotBlank
    @Override
    public void setServiceCode(String serviceCode) {
        super.setServiceCode(serviceCode);
    }
}
