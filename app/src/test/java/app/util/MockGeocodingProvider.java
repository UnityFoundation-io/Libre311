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

package app.util;

import app.geocode.GeocodeAddress;
import app.geocode.GeocodeException;
import app.geocode.GeocodingProvider;
import app.geocode.ReverseGeocodeResult;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

/**
 * Mock implementation of GeocodingProvider for testing.
 * Returns predictable fixture data without making external HTTP calls.
 *
 * Special coordinates for testing error scenarios:
 * - lat=0.0, lon=0.0: Throws GeocodeException to simulate provider failure
 */
@Singleton
@Replaces(GeocodingProvider.class)
public class MockGeocodingProvider implements GeocodingProvider {

	public static final String MOCK_DISPLAY_NAME = "100 Market Street, St. Louis, St. Louis City, Missouri, 63101, United States";
	public static final String PROVIDER_NAME = "mock";

	// Sentinel coordinates that trigger an error for testing
	public static final double ERROR_LAT = 0.0;
	public static final double ERROR_LON = 0.0;

	@Override
	public ReverseGeocodeResult reverseGeocode(double lat, double lon) {
		if (lat == ERROR_LAT && lon == ERROR_LON) {
			throw new GeocodeException("Simulated geocoding provider failure");
		}

		return new ReverseGeocodeResult(
			MOCK_DISPLAY_NAME,
			new GeocodeAddress(
				"100",
				"Market Street",
				null,
				"St. Louis",
				"St. Louis City",
				"Missouri",
				"63101",
				"United States",
				"us"
			),
			lat,
			lon,
			PROVIDER_NAME
		);
	}
}