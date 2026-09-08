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

package app.model.service.group;

import app.model.jurisdiction.Jurisdiction;
import app.model.jurisdiction.JurisdictionRepository;
import app.util.DbCleanup;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ServiceGroup entity, specifically ensuring proper ID generation strategy.
 *
 * Issue #325: ServiceGroup previously used GenerationType.AUTO which caused duplicate key errors
 * when the hibernate_sequence got out of sync with existing data.
 */
@MicronautTest(transactional = false)
public class ServiceGroupTest {

	@Inject
	ServiceGroupRepository serviceGroupRepository;

	@Inject
	JurisdictionRepository jurisdictionRepository;

	@Inject
	DbCleanup dbCleanup;

	private Jurisdiction testJurisdiction;

	@BeforeEach
	void setup() {
		testJurisdiction = jurisdictionRepository.save(new Jurisdiction("testcity.gov", 1L));
	}

	@AfterEach
	void teardown() {
		dbCleanup.cleanupAll();
	}

	/**
	 * Verifies that ServiceGroup uses GenerationType.IDENTITY for ID generation.
	 *
	 * This is a regression test for issue #325. Using GenerationType.AUTO with MySQL
	 * causes the hibernate_sequence table to get out of sync when data is seeded or
	 * imported with explicit IDs, resulting in duplicate key errors.
	 *
	 * GenerationType.IDENTITY uses the database's native auto-increment mechanism,
	 * which is more reliable and consistent with other entities in the codebase.
	 */
	@Test
	void shouldUseIdentityGenerationStrategy() throws NoSuchFieldException {
		Field idField = ServiceGroup.class.getDeclaredField("id");
		GeneratedValue annotation = idField.getAnnotation(GeneratedValue.class);

		assertNotNull(annotation, "ServiceGroup.id must have @GeneratedValue annotation");
		assertEquals(
			GenerationType.IDENTITY,
			annotation.strategy(),
			"ServiceGroup must use GenerationType.IDENTITY to avoid duplicate key errors (see issue #325)"
		);
	}

	/**
	 * Verifies that multiple ServiceGroup entities can be created without duplicate key errors.
	 *
	 * This integration test ensures the ID generation works correctly in practice.
	 * With GenerationType.AUTO, this could fail if the hibernate_sequence gets out of sync.
	 */
	@Test
	void shouldCreateMultipleGroupsWithUniqueIds() {
		Set<Long> ids = new HashSet<>();
		int groupCount = 5;

		for (int i = 0; i < groupCount; i++) {
			ServiceGroup group = new ServiceGroup("Test Group " + i, testJurisdiction);
			ServiceGroup saved = serviceGroupRepository.save(group);

			assertNotNull(saved.getId(), "Saved group should have an ID");
			assertTrue(ids.add(saved.getId()), "Each group should have a unique ID");
		}

		assertEquals(groupCount, ids.size(), "All groups should have unique IDs");
		assertEquals(groupCount, serviceGroupRepository.count());
	}

	/**
	 * Verifies that ServiceGroup can be created after deleting existing groups.
	 *
	 * This tests a common scenario where duplicate key errors occur: when data is deleted
	 * and recreated, the sequence may not account for previously used IDs.
	 */
	@Test
	void shouldCreateGroupsAfterDeletingExistingOnes() {
		ServiceGroup group1 = serviceGroupRepository.save(new ServiceGroup("Group 1", testJurisdiction));
		ServiceGroup group2 = serviceGroupRepository.save(new ServiceGroup("Group 2", testJurisdiction));
		Long id1 = group1.getId();
		Long id2 = group2.getId();

		serviceGroupRepository.delete(group1);
		serviceGroupRepository.delete(group2);

		ServiceGroup group3 = serviceGroupRepository.save(new ServiceGroup("Group 3", testJurisdiction));
		ServiceGroup group4 = serviceGroupRepository.save(new ServiceGroup("Group 4", testJurisdiction));

		assertNotNull(group3.getId());
		assertNotNull(group4.getId());
		assertNotEquals(group3.getId(), group4.getId(), "New groups should have different IDs");
	}
}
