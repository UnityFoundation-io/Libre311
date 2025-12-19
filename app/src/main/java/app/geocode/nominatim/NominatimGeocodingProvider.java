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

package app.geocode.nominatim;

import app.geocode.GeocodeAddress;
import app.geocode.GeocodingProvider;
import app.geocode.ReverseGeocodeResult;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nominatim implementation of GeocodingProvider.
 * Uses OpenStreetMap's Nominatim service for geocoding.
 */
@Singleton
@Requires(property = "libre311.geocoding.provider", value = "nominatim", defaultValue = "nominatim")
public class NominatimGeocodingProvider implements GeocodingProvider {

	private static final Logger LOG = LoggerFactory.getLogger(NominatimGeocodingProvider.class);

	private static final String USER_AGENT = "Libre311/1.0 (https://github.com/UnityFoundation-io/Libre311)";
	private static final String PROVIDER_NAME = "nominatim";

	private final NominatimClient nominatimClient;

	public NominatimGeocodingProvider(NominatimClient nominatimClient) {
		this.nominatimClient = nominatimClient;
		LOG.debug("NominatimGeocodingProvider initialized");
	}

	@Override
	public ReverseGeocodeResult reverseGeocode(double lat, double lon) {
		LOG.debug("Calling Nominatim API for reverse geocode: lat={}, lon={}", lat, lon);
		try {
			NominatimReverseResponse response = nominatimClient.reverseGeocode(lat, lon, USER_AGENT);
			LOG.debug("Nominatim raw response: placeId={}, displayName={}, address={}",
				response.placeId(), response.displayName(), response.address());
			ReverseGeocodeResult result = mapToResult(response, lat, lon);
			LOG.debug("Mapped result: displayName={}, address={}", result.displayName(), result.address());
			return result;
		} catch (HttpClientResponseException e) {
			LOG.warn("Nominatim returned error status {} for lat={}, lon={}: {}",
				e.getStatus().getCode(), lat, lon, e.getMessage());
			return createFallbackResult(lat, lon);
		} catch (HttpClientException e) {
			LOG.warn("Failed to connect to Nominatim for lat={}, lon={}: {}", lat, lon, e.getMessage());
			return createFallbackResult(lat, lon);
		} catch (Exception e) {
			LOG.error("Unexpected error calling Nominatim API for lat={}, lon={}: {}",
				lat, lon, e.getMessage(), e);
			return createFallbackResult(lat, lon);
		}
	}

	private ReverseGeocodeResult createFallbackResult(double lat, double lon) {
		String displayName = String.format("Location: %.6f, %.6f", lat, lon);
		return new ReverseGeocodeResult(displayName, null, lat, lon, PROVIDER_NAME);
	}

	private ReverseGeocodeResult mapToResult(NominatimReverseResponse response, double lat, double lon) {
		NominatimAddress addr = response.address();

		GeocodeAddress address = addr != null
			? new GeocodeAddress(
				addr.houseNumber(),
				addr.road(),
				addr.neighbourhood(),
				resolveCity(addr),
				addr.county(),
				addr.state(),
				addr.postcode(),
				addr.country(),
				addr.countryCode()
			)
			: null;

		return new ReverseGeocodeResult(
			response.displayName(),
			address,
			parseDoubleSafe(response.lat(), lat),
			parseDoubleSafe(response.lon(), lon),
			PROVIDER_NAME
		);
	}

	/**
	 * Safely parse a string to double, returning fallback if parsing fails.
	 */
	private double parseDoubleSafe(String value, double fallback) {
		if (value == null) {
			return fallback;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			LOG.warn("Failed to parse coordinate value '{}', using fallback: {}", value, fallback);
			return fallback;
		}
	}

	/**
	 * Resolve city from various Nominatim address fields.
	 * Nominatim may return city, town, village, or municipality depending on location.
	 */
	private String resolveCity(NominatimAddress addr) {
		if (addr.city() != null) return addr.city();
		if (addr.town() != null) return addr.town();
		if (addr.village() != null) return addr.village();
		return addr.municipality();
	}
}