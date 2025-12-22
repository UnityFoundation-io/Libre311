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

import app.geocode.ReverseGeocodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NominatimGeocodingProvider mapping logic.
 * Uses stub implementations to test mapping with fixture data.
 */
class NominatimGeocodingProviderTest {

	@Test
	void testMapsAllFieldsCorrectly() {
		NominatimAddress address = new NominatimAddress(
			"100",           // houseNumber
			"Market Street", // road
			"Downtown",      // neighbourhood
			"St. Louis",     // city
			null,            // town
			null,            // village
			null,            // municipality
			"St. Louis City",// county
			"Missouri",      // state
			"63101",         // postcode
			"United States", // country
			"us"             // countryCode
		);

		NominatimReverseResponse response = new NominatimReverseResponse(
			12345L,
			"Data © OpenStreetMap contributors",
			"way",
			67890L,
			"38.6270025",
			"-90.1994042",
			"building",
			"commercial",
			30,
			0.5,
			"building",
			"City Hall",
			"100 Market Street, St. Louis, Missouri, 63101, United States",
			address
		);

		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(38.6270025, -90.1994042);

		assertNotNull(result);
		assertEquals("100 Market Street, St. Louis, Missouri, 63101, United States", result.displayName());
		assertEquals(38.6270025, result.latitude(), 0.0001);
		assertEquals(-90.1994042, result.longitude(), 0.0001);
		assertEquals("nominatim", result.provider());

		assertNotNull(result.address());
		assertEquals("100", result.address().streetNumber());
		assertEquals("Market Street", result.address().street());
		assertEquals("Downtown", result.address().neighborhood());
		assertEquals("St. Louis", result.address().city());
		assertEquals("St. Louis City", result.address().county());
		assertEquals("Missouri", result.address().state());
		assertEquals("63101", result.address().postalCode());
		assertEquals("United States", result.address().country());
		assertEquals("us", result.address().countryCode());
	}

	@Test
	void testResolveCityFallsBackToTown() {
		NominatimAddress address = new NominatimAddress(
			"1",           // houseNumber
			"Main St",     // road
			null,          // neighbourhood
			null,          // city
			"Springfield", // town
			null,          // village
			null,          // municipality
			null,          // county
			"Pennsylvania",// state
			"19000",       // postcode
			"United States", // country
			"us"           // countryCode
		);

		NominatimReverseResponse response = createResponseWithAddress(address);
		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(40.0, -75.0);

		assertEquals("Springfield", result.address().city());
	}

	@Test
	void testResolveCityFallsBackToVillage() {
		NominatimAddress address = new NominatimAddress(
			"1",             // houseNumber
			"Main St",       // road
			null,            // neighbourhood
			null,            // city
			null,            // town
			"Small Village", // village
			null,            // municipality
			null,            // county
			"Pennsylvania",  // state
			"19000",         // postcode
			"United States", // country
			"us"             // countryCode
		);

		NominatimReverseResponse response = createResponseWithAddress(address);
		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(40.0, -75.0);

		assertEquals("Small Village", result.address().city());
	}

	@Test
	void testResolveCityFallsBackToMunicipality() {
		NominatimAddress address = new NominatimAddress(
			"1",                  // houseNumber
			"Main St",            // road
			null,                 // neighbourhood
			null,                 // city
			null,                 // town
			null,                 // village
			"Rural Municipality", // municipality
			null,                 // county
			"Pennsylvania",       // state
			"19000",              // postcode
			"United States",      // country
			"us"                  // countryCode
		);

		NominatimReverseResponse response = createResponseWithAddress(address);
		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(40.0, -75.0);

		assertEquals("Rural Municipality", result.address().city());
	}

	@Test
	void testHandlesNullAddress() {
		NominatimReverseResponse response = new NominatimReverseResponse(
			12345L, "license", "way", 67890L,
			"38.6270025", "-90.1994042",
			"building", "commercial", 30, 0.5, "building", "Unknown",
			"Unknown Location",
			null  // address is null
		);

		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(38.6270025, -90.1994042);

		assertNotNull(result);
		assertEquals("Unknown Location", result.displayName());
		assertNull(result.address());
		assertEquals(38.6270025, result.latitude(), 0.0001);
		assertEquals(-90.1994042, result.longitude(), 0.0001);
	}

	@Test
	void testFallsBackToInputCoordinatesWhenResponseLatLonNull() {
		double inputLat = 38.6270025;
		double inputLon = -90.1994042;

		NominatimReverseResponse response = new NominatimReverseResponse(
			12345L, "license", "way", 67890L,
			null,  // lat is null
			null,  // lon is null
			"building", "commercial", 30, 0.5, "building", "Test",
			"Test Location",
			null
		);

		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(inputLat, inputLon);

		assertEquals(inputLat, result.latitude(), 0.0001);
		assertEquals(inputLon, result.longitude(), 0.0001);
	}

	@Test
	void testUsesResponseCoordinatesWhenAvailable() {
		double inputLat = 38.0;
		double inputLon = -90.0;
		double responseLat = 38.6270025;
		double responseLon = -90.1994042;

		NominatimReverseResponse response = new NominatimReverseResponse(
			12345L, "license", "way", 67890L,
			String.valueOf(responseLat),
			String.valueOf(responseLon),
			"building", "commercial", 30, 0.5, "building", "Test",
			"Test Location",
			null
		);

		NominatimClient stubClient = (lat, lon, userAgent) -> response;
		NominatimGeocodingProvider provider = new NominatimGeocodingProvider(stubClient);

		ReverseGeocodeResult result = provider.reverseGeocode(inputLat, inputLon);

		assertEquals(responseLat, result.latitude(), 0.0001);
		assertEquals(responseLon, result.longitude(), 0.0001);
	}

	private NominatimReverseResponse createResponseWithAddress(NominatimAddress address) {
		return new NominatimReverseResponse(
			12345L, "license", "way", 67890L,
			"40.0", "-75.0",
			"building", "residential", 30, 0.5, "building", "Test",
			"Test Location",
			address
		);
	}
}