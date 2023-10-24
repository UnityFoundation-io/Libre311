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

package app.safesearch;

import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Singleton
public class GoogleImageSafeSearchService {

    @Inject
    SafeSearchClient client;

    @Property(name = "app.safesearch.key")
    protected String key;

    public boolean imageIsExplicit(String image) {
        boolean isImageExplicit = true;

        try {
            Map payload = Map.of(
                    "requests", List.of(
                            Map.of(
                                    "image", Map.of("content", image),
                                    "features", List.of(Map.of("type", "SAFE_SEARCH_DETECTION"))
                            )
                    )
            );
            Map map = client.classifyImage(payload, key);
            Map safeSearchAnnotationsMap = (Map) ((Map) ((List) map.get("responses")).get(0)).get("safeSearchAnnotation");
            isImageExplicit = safeSearchAnnotationsMap.entrySet().stream()
                    .filter((Predicate<Map.Entry<String, String>>) filterEntry -> {
                        String k = filterEntry.getKey();
                        return k.equals("adult") || k.equals("violence") || k.equals("racy");
                    })
                    .anyMatch((Predicate<Map.Entry<String, String>>) matchEntry -> {
                        String v = matchEntry.getValue();
                        return v.equals("LIKELY") || v.equals("VERY_LIKELY");
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isImageExplicit;
    }
}
