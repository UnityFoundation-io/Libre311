<script>
  import { Loader } from "@googlemaps/js-api-loader";
  import { onMount } from "svelte";
  import logo from "$lib/logo.png";
  import userCurrentLocation from "../../stores/userCurrentLocation";

  const loader = new Loader({
    apiKey: "AIzaSyD3yaXnm9G-j8kgeL6ucQxtRAyjF5-h7ZE",
    version: "weekly",
    libraries: ["places"],
  });

  let map, geocoder, bounds, inputIssueAddress;
  let zoom = 15;
  let markers = [];

  onMount(() => {
    console.log("$userCurrentLocation", $userCurrentLocation);
    loader.load().then(async (google) => {
      map = new google.maps.Map(document.getElementById("map"), {
        zoom: zoom,
        center: {
          lat: $userCurrentLocation.lat,
          lng: $userCurrentLocation.lng,
        },
      });

      geocoder = new google.maps.Geocoder();
      bounds = new google.maps.LatLngBounds();

      inputIssueAddress = document.getElementById("pac-input");
      const searchBox = new google.maps.places.SearchBox(inputIssueAddress);

      map.controls[google.maps.ControlPosition.TOP_LEFT].push(
        inputIssueAddress
      );

      map.addListener("bounds_changed", () => {
        searchBox.setBounds(map.getBounds());
      });

      searchBox.addListener("places_changed", () => {
        const places = searchBox.getPlaces();

        if (places.length == 0) {
          return;
        }

        clearMarkers();

        places.forEach((place) => {
          // issueAddress.set(place.formatted_address);

          if (!place.geometry || !place.geometry.location) {
            console.log("Returned place contains no geometry");
            return;
          }

          const icon = {
            size: new google.maps.Size(71, 71),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(17, 34),
            scaledSize: new google.maps.Size(55, 55),
          };

          const marker = new google.maps.Marker({
            map,
            icon,
            title: place.name,
            position: place.geometry.location,
            title: "Issue's Location",
          });

          // Create a marker for each place.
          markers.push(marker);

          if (place.geometry.viewport) {
            // Only geocodes have viewport.
            bounds.union(place.geometry.viewport);
          } else {
            bounds.extend(place.geometry.location);
          }
        });
        map.fitBounds(bounds);
      });
    });
  });
</script>

<svelte:head>
  <title>Reported Issues | We Move</title>
  <meta
    name="description"
    content="We Move Share the location of a walking, biking, or transit issues"
  />
</svelte:head>

<div class="content">
  <!-- <div>
    <img
      src="{logo}"
      alt="we move logo"
      height="100rem"
      style="filter: drop-shadow(3px 3px 3px black); margin-left: 2.5rem; margin: 1rem 0 0.5rem 0"
    />
  </div> -->

  <input id="pac-input" placeholder="Find address or place" type="text" />
  <div id="map"></div>

  <div class="filters">
    <div style="font-weight: 550; padding: 0.5rem 0 0.5rem 0.5rem">Filters</div>

    <select
      on:change="{(e) => {
        console.log(e.target.value);
      }}"
    >
      <option disabled selected value="">Status</option>
      <option value="Sidewalk">Sidewalk</option>
      <option value="Bike Lane">Bike Lane</option>
      <option value="Bus Stop">Bus Stop</option>
      <option value="Traffic Light">Traffic Light</option>
    </select>

    <select
      on:change="{(e) => {
        console.log(e.target.value);
      }}"
    >
      <option disabled selected value="">Issue Type</option>
      <option value="Sidewalk">Sidewalk</option>
      <option value="Bike Lane">Bike Lane</option>
      <option value="Bus Stop">Bus Stop</option>
      <option value="Traffic Light">Traffic Light</option>
    </select>

    <select
      on:change="{(e) => {
        console.log(e.target.value);
      }}"
    >
      <option disabled selected value="">Reported By</option>
      <option value="Sidewalk">Sidewalk</option>
      <option value="Bike Lane">Bike Lane</option>
      <option value="Bus Stop">Bus Stop</option>
      <option value="Traffic Light">Traffic Light</option>
    </select>

    <select
      on:change="{(e) => {
        console.log(e.target.value);
      }}"
    >
      <option disabled selected value="">Date Range</option>
      <option value="Sidewalk">Sidewalk</option>
      <option value="Bike Lane">Bike Lane</option>
      <option value="Bus Stop">Bus Stop</option>
      <option value="Traffic Light">Traffic Light</option>
    </select>
  </div>
</div>

<style>
  .content {
    margin: 0 auto;
    width: 85.5vw;
    height: 100vh;
  }

  .filters {
    width: 75vw;
    margin: 0 auto;
    background-color: white;
  }

  #map {
    width: 75vw;
    height: 55vh;
    margin: 0 auto;
    margin-top: 3rem;
    align-items: center;
  }

  #pac-input {
    margin-top: 0.6rem;
    padding-left: 0.5rem;
    height: 2.18rem;
    width: 23rem;
  }
</style>
