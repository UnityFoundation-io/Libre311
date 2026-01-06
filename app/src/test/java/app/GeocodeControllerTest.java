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

package app;

import app.geocode.ReverseGeocodeResult;
import app.util.MockGeocodingProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class GeocodeControllerTest {

	@Inject
	@Client("/api/geocode")
	HttpClient client;

	@Test
	void testReverseGeocodeReturnsValidResponse() {
		double lat = 38.6270025;
		double lon = -90.1994042;

		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=" + lat + "&lon=" + lon);
		ReverseGeocodeResult result = client.toBlocking().retrieve(request, ReverseGeocodeResult.class);

		assertNotNull(result);
		assertEquals(MockGeocodingProvider.MOCK_DISPLAY_NAME, result.displayName());
		assertEquals(lat, result.latitude(), 0.0001);
		assertEquals(lon, result.longitude(), 0.0001);
		assertEquals(MockGeocodingProvider.PROVIDER_NAME, result.provider());

		assertNotNull(result.address());
		assertEquals("100", result.address().streetNumber());
		assertEquals("Market Street", result.address().street());
		assertEquals("St. Louis", result.address().city());
		assertEquals("Missouri", result.address().state());
		assertEquals("63101", result.address().postalCode());
		assertEquals("United States", result.address().country());
		assertEquals("us", result.address().countryCode());
	}

	@Test
	void testReverseGeocodeWithDifferentCoordinates() {
		double lat = 40.7128;
		double lon = -74.0060;

		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=" + lat + "&lon=" + lon);
		ReverseGeocodeResult result = client.toBlocking().retrieve(request, ReverseGeocodeResult.class);

		assertNotNull(result);
		// Mock provider returns same address but with requested coordinates
		assertEquals(lat, result.latitude(), 0.0001);
		assertEquals(lon, result.longitude(), 0.0001);
	}

	@Test
	void testReverseGeocodeWithMissingLatReturns400() {
		HttpRequest<?> request = HttpRequest.GET("/reverse?lon=-90.1994042");

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void testReverseGeocodeWithMissingLonReturns400() {
		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=38.6270025");

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void testReverseGeocodeWithInvalidLatReturns400() {
		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=invalid&lon=-90.1994042");

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void testReverseGeocodeWhenProviderFailsReturns503() {
		// Use sentinel coordinates that trigger mock provider to throw
		double lat = MockGeocodingProvider.ERROR_LAT;
		double lon = MockGeocodingProvider.ERROR_LON;

		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=" + lat + "&lon=" + lon);

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
	}

	@Test
	void testReverseGeocodeWithLatOutOfRangeReturns400() {
		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=91&lon=-90.0");

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}

	@Test
	void testReverseGeocodeWithLonOutOfRangeReturns400() {
		HttpRequest<?> request = HttpRequest.GET("/reverse?lat=38.0&lon=181");

		HttpClientResponseException exception = assertThrows(
			HttpClientResponseException.class,
			() -> client.toBlocking().retrieve(request, ReverseGeocodeResult.class)
		);

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
	}
}