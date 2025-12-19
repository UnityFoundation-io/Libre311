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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Internal DTO for parsing Nominatim reverse geocode response (jsonv2 format).
 */
@Serdeable
@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimReverseResponse(
	@JsonProperty("place_id") Long placeId,
	String licence,
	@JsonProperty("osm_type") String osmType,
	@JsonProperty("osm_id") Long osmId,
	String lat,
	String lon,
	String category,
	String type,
	@JsonProperty("place_rank") Integer placeRank,
	Double importance,
	String addresstype,
	String name,
	@JsonProperty("display_name") String displayName,
	NominatimAddress address
) {}