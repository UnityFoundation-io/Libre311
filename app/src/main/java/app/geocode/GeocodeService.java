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

package app.geocode;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service layer for geocoding operations.
 * Delegates to the configured GeocodingProvider implementation.
 */
@Singleton
public class GeocodeService {

	private static final Logger LOG = LoggerFactory.getLogger(GeocodeService.class);

	private final GeocodingProvider geocodingProvider;

	public GeocodeService(GeocodingProvider geocodingProvider) {
		this.geocodingProvider = geocodingProvider;
		LOG.debug("GeocodeService initialized with provider: {}", geocodingProvider.getClass().getSimpleName());
	}

	/**
	 * Perform reverse geocoding to get an address from coordinates.
	 *
	 * @param lat latitude
	 * @param lon longitude
	 * @return the geocoding result with address details
	 */
	public ReverseGeocodeResult reverseGeocode(double lat, double lon) {
		LOG.debug("Delegating reverse geocode to provider for lat={}, lon={}", lat, lon);
		ReverseGeocodeResult result = geocodingProvider.reverseGeocode(lat, lon);
		LOG.debug("Provider returned result with provider name: {}", result.provider());
		return result;
	}
}