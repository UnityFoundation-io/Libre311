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

package app.recaptcha;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class ReCaptchaService {

    @Inject
    ReCaptchaClient client;

    @Property(name = "app.recaptcha.secret")
    protected String secret;

    public Boolean verifyReCaptcha(String response) {
        Map map = client.verifyReCaptcha(this.secret, response);
        Boolean success = (Boolean) map.get("success");
        return success;
    }
}
