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

import app.exception.Libre311BaseException;
import io.micronaut.http.HttpStatus;

/**
 * Exception thrown when geocoding operations fail.
 * Returns 503 Service Unavailable to indicate the external geocoding service is not available.
 */
public class GeocodeException extends Libre311BaseException {

	public GeocodeException(String message) {
		super(message, HttpStatus.SERVICE_UNAVAILABLE);
	}

	public GeocodeException(String message, Throwable cause) {
		super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
	}
}