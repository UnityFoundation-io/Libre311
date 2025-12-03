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

package app.exception;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LogrefGeneratorTest {

    @Test
    void generate_shouldReturnCorrectLength() {
        String result = LogrefGenerator.generate(8);
        assertEquals(8, result.length());
    }

    @Test
    void generate_shouldReturnDifferentLengths() {
        String short4 = LogrefGenerator.generate(4);
        String medium8 = LogrefGenerator.generate(8);
        String long16 = LogrefGenerator.generate(16);

        assertEquals(4, short4.length());
        assertEquals(8, medium8.length());
        assertEquals(16, long16.length());
    }

    @Test
    void generate_shouldContainOnlyUppercaseAlphanumeric() {
        String result = LogrefGenerator.generate(100);
        
        for (char c : result.toCharArray()) {
            assertTrue(Character.isUpperCase(c) || Character.isDigit(c),
                    "Character '" + c + "' is not uppercase or digit");
        }
    }

    @Test
    void generate_shouldProduceDifferentResults() {
        Set<String> generated = new HashSet<>();
        
        // Generate 1000 identifiers - should be highly unlikely to get duplicates
        for (int i = 0; i < 1000; i++) {
            String identifier = LogrefGenerator.generate(8);
            generated.add(identifier);
        }
        
        // With 36^8 possible combinations, we should have close to 1000 unique values
        assertTrue(generated.size() > 990, 
                "Expected at least 990 unique identifiers, got " + generated.size());
    }

    @Test
    void generate_shouldHandleZeroLength() {
        String result = LogrefGenerator.generate(0);
        assertEquals("", result);
    }

    @Test
    void generate_shouldHandleLargeLength() {
        String result = LogrefGenerator.generate(1000);
        assertEquals(1000, result.length());
    }

    @Test
    void generate_shouldBeThreadSafe() throws InterruptedException {
        Set<String> results = new HashSet<>();
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String identifier = LogrefGenerator.generate(8);
                    synchronized (results) {
                        results.add(identifier);
                    }
                }
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Should have generated many unique identifiers across threads
        assertTrue(results.size() > 900, 
                "Expected at least 900 unique identifiers from concurrent generation");
    }
}
