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

package app.storage;

import app.storage.StorageUrlUtil;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "app.image-storage.bucket-url-format", value = "https://storage.example.com/%s")
@Property(name = "app.image-storage.bucket", value = "test-bucket")
class StorageUrlUtilTest {

    @Inject
    StorageUrlUtil storageUrlUtil;

    @Test
    void getBucketUrlString_shouldFormatUrlCorrectly() {
        String result = storageUrlUtil.getBucketUrlString();
        
        assertEquals("https://storage.example.com/test-bucket", result);
    }

    @Test
    void getBucketUrlString_shouldBeConsistent() {
        String result1 = storageUrlUtil.getBucketUrlString();
        String result2 = storageUrlUtil.getBucketUrlString();
        
        assertEquals(result1, result2);
    }
}
