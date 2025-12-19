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

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for geocoding operations.
 * Provides a proxy to external geocoding services to avoid CORS issues.
 */
@Controller("/api/geocode")
@Secured(SecurityRule.IS_ANONYMOUS)
public class GeocodeController {

	private static final Logger LOG = LoggerFactory.getLogger(GeocodeController.class);

	private final GeocodeService geocodeService;

	public GeocodeController(GeocodeService geocodeService) {
		this.geocodeService = geocodeService;
	}

	/**
	 * Reverse geocode coordinates to an address.
	 *
	 * @param lat latitude
	 * @param lon longitude
	 * @return the geocoding result with address details
	 */
	@Get("/reverse")
	@Produces(MediaType.APPLICATION_JSON)
	@ExecuteOn(TaskExecutors.IO)
	public ReverseGeocodeResult reverseGeocode(
		@QueryValue double lat,
		@QueryValue double lon
	) {
		LOG.debug("Reverse geocode request received: lat={}, lon={}", lat, lon);
		validateCoordinates(lat, lon);
		ReverseGeocodeResult result = geocodeService.reverseGeocode(lat, lon);
		LOG.debug("Reverse geocode result: displayName={}, address={}", result.displayName(), result.address());
		return result;
	}

	private void validateCoordinates(double lat, double lon) {
		if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
			throw new HttpStatusException(HttpStatus.BAD_REQUEST,
				"Invalid coordinates: lat must be between -90 and 90, lon must be between -180 and 180");
		}
	}
}