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

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
@InterceptorBean(CheckRecaptcha.class)
public class CheckRecaptchaInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger LOG = LoggerFactory.getLogger(CheckRecaptchaInterceptor.class);

    private final ReCaptchaService reCaptchaService;

    public CheckRecaptchaInterceptor(ReCaptchaService reCaptchaService) {
        this.reCaptchaService = reCaptchaService;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        LOG.debug("Intercepted call, looking for recaptcha class");
        Optional<?> recaptchaParam = context.getParameters()
                .values()
                .stream()
                .filter(entry -> entry.getValue() instanceof RecaptchaRequest)
                .map(entry -> entry.getValue())
                .findFirst();
        if (recaptchaParam.isPresent()) {
            reCaptchaService.verifyReCaptcha((RecaptchaRequest)recaptchaParam.get());
        } else {
            LOG.debug("No recaptcha class found, looking for recaptcha string");
            var string = context.getParameters()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals("gRecaptchaResponse"))
                    .map(entry -> entry.getValue())
                    .map(entry -> entry.getValue())
                    .findFirst();
            if (string.isPresent()){
                reCaptchaService.verifyReCaptcha((String)string.get());
            } else {
                LOG.debug("No recaptcha info found");
            }
        }
        return context.proceed();
    }
}
