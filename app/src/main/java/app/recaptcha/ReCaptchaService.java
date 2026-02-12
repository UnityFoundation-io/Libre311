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

import app.exception.Libre311BaseException;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Singleton
public class ReCaptchaService {

    private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaService.class);

    @Inject
    ReCaptchaClient client;

    @Property(name = "app.recaptcha.secret")
    protected String secret;

    static class RecaptchaVerificationFailed extends Libre311BaseException {
        public RecaptchaVerificationFailed() {
            super("ReCaptcha verification failed.", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyReCaptcha(RecaptchaRequest recaptchaRequest){
        verifyReCaptcha(recaptchaRequest.getgRecaptchaResponse());
    }

    public void verifyReCaptcha(String response) {
        LOG.debug("Verifying recaptcha, response: {}", response);
        Map<Object, Object> map = client.verifyReCaptcha(this.secret, response);
        Boolean success = (Boolean) map.get("success");
        if (!success){
            throw new RecaptchaVerificationFailed();
        }
        LOG.debug("recaptcha verified, response: {}", response);
    }
}
