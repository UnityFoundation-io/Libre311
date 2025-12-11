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

package app.service.jurisdiction;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Provides default policy content (privacy policy and terms of use) from classpath resources.
 * Content is lazily loaded on first access and cached for the lifetime of the application.
 */
@Singleton
public class DefaultPolicyContentProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPolicyContentProvider.class);

    private static final String DEFAULT_PRIVACY_POLICY_PATH = "defaults/privacy.md";
    private static final String DEFAULT_TERMS_OF_USE_PATH = "defaults/terms.md";

    private final Supplier<String> privacyPolicySupplier = memoize(
        () -> loadResource(DEFAULT_PRIVACY_POLICY_PATH, "Privacy Policy")
    );

    private final Supplier<String> termsOfUseSupplier = memoize(
        () -> loadResource(DEFAULT_TERMS_OF_USE_PATH, "Terms of Use")
    );

    private String loadResource(String path, String name) {
        LOG.info("Loading default {} from classpath: {}", name.toLowerCase(), path);
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                LOG.warn("Resource not found: {}", path);
            }
        } catch (IOException e) {
            LOG.error("Failed to read resource: {}", path, e);
        }
        return "# " + name + "\n\nNo default " + name.toLowerCase() + " available.";
    }

    private static <T> Supplier<T> memoize(Supplier<T> delegate) {
        AtomicReference<T> cache = new AtomicReference<>();
        return () -> {
            T val = cache.get();
            if (val == null) {
                val = delegate.get();
                cache.set(val);
            }
            return val;
        };
    }

    /**
     * Returns the default privacy policy content.
     * Content is lazily loaded on first access.
     */
    public String getDefaultPrivacyPolicy() {
        return privacyPolicySupplier.get();
    }

    /**
     * Returns the default terms of use content.
     * Content is lazily loaded on first access.
     */
    public String getDefaultTermsOfUse() {
        return termsOfUseSupplier.get();
    }
}
