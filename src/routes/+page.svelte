<script>
  import { Loader } from "@googlemaps/js-api-loader";
  import { fade, scale, blur } from "svelte/transition";
  import { quintOut } from "svelte/easing";
  import { onMount } from "svelte";
  import logo from "$lib/logo.png";
  import addSVG from "../icons/add.svg";
  import closeSVG from "../icons/close.svg";
  import searchSVG from "../icons/search.svg";
  import currentLocationSVG from "../icons/pin.svg";
  import pageForwardSVG from "../icons/pageforward.svg";
  import pageBackwardsSVG from "../icons/pagebackwards.svg";
  import pageLastSVG from "../icons/pagelast.svg";
  import cameraSVG from "../icons/camera.svg";
  import imageSVG from "../icons/image.svg";
  import sidewalkSVG from "../icons/sidewalk.svg";
  import busstopSVG from "../icons/busstop.svg";
  import bikelaneSVG from "../icons/bikelane.svg";
  import trafficlightSVG from "../icons/trafficlight.svg";
  import issueAddress from "../stores/issueAddress";
  import issueTime from "../stores/issueTime";
  import issueType from "../stores/issueType";
  import issueDetail from "../stores/issueDetail";
  import issueDescription from "../stores/issueDescription";
  import issueSubmitterName from "../stores/issueSubmitterName";
  import issueSubmitterContact from "../stores/issueSubmitterContact";
  import userCurrentLocation from "../stores/userCurrentLocation";
  import resetDate from "../stores/resetDate";
  import DateRangePicker from "../lib/DateRangePicker.svelte";
  import "$lib/global.css";

  let mockData = [
    {
      service_request_id: 638344,
      status: "closed",
      status_notes: "Duplicate request.",
      service_name: "Sidewalk",
      service_code: "006",
      description: "Sidewalk damaged",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-03-14T03:17:12-08:00",
      updated_datetime: "2010-04-14T06:37:38-08:00",
      expected_datetime: "2010-04-15T06:37:38-08:00",
      address: "780 N New Ballas Rd, Creve Coeur, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.66927,
      long: -90.44221,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Sidewalk",
      service_code: "006",
      description: "Missing access ramp",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-04-22T01:31:28-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "12140 Woodcrest Executive Dr, St. Louis, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.673912,
      long: -90.453064,
      media_url: null,
    },
    {
      service_request_id: 638349,
      status: "closed",
      status_notes: null,
      service_name: "Traffic Light",
      service_code: "006",
      description: "No audible aid",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-07-01T08:38:12-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "12545 Olive Blvd, St. Louis, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.678081,
      long: -90.462677,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Bus Stop",
      service_code: "006",
      description: "Bus is always late",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-06-29T05:12:28-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "12395 Woodline Dr, Creve Coeur, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.663673,
      long: -90.46033,
      media_url: null,
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Sidewalk",
      service_code: "006",
      description: "Missing access ramp",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-06-09T01:23:38-08:00",
      updated_datetime: "2010-06-9T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "928 Fernway Ln, St. Louis, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.677221,
      long: -90.478038,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Traffic Light",
      service_code: "006",
      description: "No audible aid",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-02-11T02:21:32-08:00",
      updated_datetime: "2010-02-11T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "12341 Promenade Ln, St. Louis, MO 63146",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.675079,
      long: -90.457593,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
    {
      service_request_id: 638349,
      status: "closed",
      status_notes: null,
      service_name: "Bus Stop",
      service_code: "006",
      description: "Bus is always late",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-03-01T06:12:13-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "12516 Whispering Hills Ln, St. Louis, MO 63146",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.680063,
      long: -90.461058,
      media_url: null,
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Traffic Light",
      service_code: "006",
      description: "Too long",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-05-15T06:37:38-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "13122 Strawberry Way, St. Louis, MO 63146",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.689134,
      long: -90.48496,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },

    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Bus Stop",
      service_code: "006",
      description: "No shelter",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-06-19T03:27:32-08:00",
      updated_datetime: "2010-02-11T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "1600 Bookbinder Dr, St. Louis, MO 63146",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.688875,
      long: -90.488404,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
    {
      service_request_id: 638349,
      status: "open",
      status_notes: null,
      service_name: "Bike Lane",
      service_code: "006",
      description: "Not present",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-08-11T02:02:03-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "1175 Mill Crossing Dr, Creve Coeur, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.681291,
      long: -90.483348,
      media_url: null,
    },
    {
      service_request_id: 638349,
      status: "closed",
      status_notes: null,
      service_name: "Bike Lane",
      service_code: "006",
      description: "Narrow",
      agency_responsible: null,
      service_notice: null,
      requested_datetime: "2022-07-05T06:37:38-08:00",
      updated_datetime: "2010-04-19T06:37:38-08:00",
      expected_datetime: "2010-04-19T06:37:38-08:00",
      address: "13031 Gallagher Rd, Creve Coeur, MO 63141",
      address_id: 545483,
      zipcode: 94122,
      lat: 38.67681,
      long: -90.482908,
      media_url:
        "https://images.pexels.com/photos/136739/pexels-photo-136739.jpeg",
    },
  ];

  let filteredMockData = mockData;
  let filterArray = [];

  // Filtering Results
  $: if (filterArray.find((filter) => filter.hasOwnProperty("issueType"))) {
    filteredMockData = mockData;

    const issueTypeFilter = filterArray.find((filter) =>
      filter.hasOwnProperty("issueType")
    )["issueType"];

    filteredMockData = filteredMockData.filter(
      (issue) => issue.service_name === issueTypeFilter
    );

    if (filterArray.length === 1) addIssuesToMap();

    if (filterArray.length === 2) {
      filterByDates();
      addIssuesToMap();
    }
  } else if (
    filterArray.length === 1 &&
    filterArray.find((filter) => filter.hasOwnProperty("dates"))
  ) {
    filteredMockData = mockData;
    filterByDates();
    addIssuesToMap();
  }

  const startRendering = 2000;

  const loader = new Loader({
    apiKey: "AIzaSyD3yaXnm9G-j8kgeL6ucQxtRAyjF5-h7ZE",
    version: "weekly",
    libraries: ["places"],
  });

  let openLogo = false,
    fadeInBackground = false,
    openWeMove = false,
    reduceBackGroundOpacity = false,
    reportNewIssue = false,
    reportNewIssueStep2 = false,
    reportNewIssueStep3 = false,
    reportNewIssueStep4 = false,
    reportNewIssueStep5 = false,
    reportNewIssueStep6 = false,
    currentStep = null,
    findReportedIssue = false;

  let backgroundSelector,
    sectionNewReport,
    map,
    geocoder,
    bounds,
    inputIssueAddress,
    issueTypeSelector,
    issueDetailSelector,
    issueTypeSelectSelector;

  let zoom = 15;
  let markers = [];

  $: if (reportNewIssueStep6)
    setTimeout(() => {
      reportNewIssueStep6 = false;
      resetState();
      scrollToTop();
    }, 4000);

  const filterByDates = () => {
    const selectedDates = filterArray.find((filter) =>
      filter.hasOwnProperty("dates")
    )["dates"];

    const filterInitialDate = new Date(selectedDates[0]);
    const filterEndingDate = new Date(selectedDates[1]);

    filteredMockData = filteredMockData.filter(
      (issue) =>
        new Date(issue.requested_datetime) > filterInitialDate &&
        new Date(issue.requested_datetime) < filterEndingDate
    );
  };

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const scrollToSection = () => {
    const y =
      sectionNewReport.getBoundingClientRect().top + window.pageYOffset + 200;
    window.scrollTo({ top: y, behavior: "smooth" });
  };

  const geocodeLatLng = (lat, lng) => {
    const latlng = { lat: parseFloat(lat), lng: parseFloat(lng) };

    geocoder.geocode({ location: latlng }, (results, status) => {
      if (status === "OK") {
        if (results[0]) {
          issueAddress.set(results[0].formatted_address);
          inputIssueAddress.value = results[0].formatted_address;
        } else {
          console.log("No results found");
        }
      } else {
        console.log(`Geocoder failed due to: ${status}`);
      }
    });
  };

  const setNewCenter = (lat, lng) => {
    let newCenter = new google.maps.LatLng(lat, lng);
    map.setCenter(newCenter);
    setNewZoom(15);
  };

  const setNewZoom = (zoomLevel) => {
    map.setZoom(zoomLevel);
  };

  const successCallback = (position) => {
    $userCurrentLocation = {
      lat: position.coords.latitude,
      lng: position.coords.longitude,
    };

    setNewCenter($userCurrentLocation.lat, $userCurrentLocation.lng);
    const marker = new google.maps.Marker({
      position: {
        lat: $userCurrentLocation.lat,
        lng: $userCurrentLocation.lng,
      },
      icon: {
        scaledSize: new google.maps.Size(55, 55),
        url: currentLocationSVG,
      },
      map: map,
      draggable: true,
      title: "Issue's Location",
    });

    markers.push(marker);

    google.maps.event.addListener(marker, "dragend", function (evt) {
      const lat = evt.latLng.lat();
      const lng = evt.latLng.lng();

      setNewCenter(lat, lng);
      geocodeLatLng(lat, lng);
    });

    issueTime.set(convertDate(position.timestamp));

    geocodeLatLng($userCurrentLocation.lat, $userCurrentLocation.lng);
  };

  const errorCallback = (error) => {
    console.log(error);
  };

  const clearMarkers = () => {
    markers.forEach((marker) => {
      marker.setMap(null);
    });
    markers = [];
  };

  // From Unix Epoch to Current Time
  const convertDate = (unixTimestamp) => {
    const date = new Date(unixTimestamp);
    return date.toLocaleString();
  };

  const resetState = () => {
    setTimeout(() => scrollToTop(), 100);
    reduceBackGroundOpacity = true;
    clearMarkers();
    issueAddress.set();
    issueDescription.set();
    $issueDetail = null;
    $issueType = null;
    // currentStep = null;
    setTimeout(() => (currentStep = null), 700);
  };

  const formatDate = (dateString) => {
    const months = [
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December",
    ];

    const date = new Date(dateString);
    const day = date.getDate();
    const monthIndex = date.getMonth();
    const year = date.getFullYear();
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const seconds = date.getSeconds();

    const formattedDate = `${
      months[monthIndex]
    } ${day}, ${year} ${hours}:${minutes.toString().padStart(2, "0")}:${seconds
      .toString()
      .padStart(2, "0")}`;

    return formattedDate;
  };

  const addIssuesToMap = async () => {
    clearMarkers();

    if (filteredMockData && filteredMockData.length > 0) {
      filteredMockData.forEach((issue) => {
        let marker, urlIcon;

        switch (issue.service_name) {
          case "Sidewalk":
            urlIcon = sidewalkSVG;
            break;
          case "Bus Stop":
            urlIcon = busstopSVG;
            break;
          case "Traffic Light":
            urlIcon = trafficlightSVG;
            break;
          case "Bike Lane":
            urlIcon = bikelaneSVG;
            break;
        }

        marker = new google.maps.Marker({
          position: {
            lat: parseFloat(issue.lat),
            lng: parseFloat(issue.long),
          },
          map: map,
          title: issue.name,
          icon: {
            scaledSize: new google.maps.Size(40, 40),
            url: urlIcon,
          },
        });

        markers.push(marker);
      });

      setTimeout(() => {
        calculateBoundsAroundMarkers();
      }, 100);
    }
  };

  const calculateBoundsAroundMarkers = () => {
    if (markers && bounds) {
      let lat, lng;

      for (let i = 0; i < markers.length; i++) {
        lat = markers[i].position.lat();
        lng = markers[i].position.lng();
        bounds.extend({ lat, lng });
      }
      setNewCenter(bounds.getCenter());
      map.fitBounds(bounds);
    }
  };

  onMount(() => {
    scrollToTop();

    // Trigger the Svelte Transitions
    fadeInBackground = true;
    openLogo = true;
    openWeMove = true;

    loader.load().then(async (google) => {
      map = new google.maps.Map(document.getElementById("map"), {
        zoom: zoom,
        center: { lat: 38.6740015313782, lng: -90.453269188364 },
      });

      geocoder = new google.maps.Geocoder();
      bounds = new google.maps.LatLngBounds();

      inputIssueAddress = document.getElementById("pac-input");
      const searchBox = new google.maps.places.SearchBox(inputIssueAddress);

      map.controls[google.maps.ControlPosition.TOP_LEFT].push(
        inputIssueAddress
      );

      // Bias the SearchBox results towards current map's viewport.
      map.addListener("bounds_changed", () => {
        searchBox.setBounds(map.getBounds());
      });

      // Listen for the event fired when the user selects a prediction and retrieve
      // more details for that place.
      searchBox.addListener("places_changed", () => {
        const places = searchBox.getPlaces();

        if (places.length == 0) {
          return;
        }

        // Clear out the old markers.
        clearMarkers();

        places.forEach((place) => {
          issueAddress.set(place.formatted_address);

          if (!place.geometry || !place.geometry.location) {
            console.log("Returned place contains no geometry");
            return;
          }

          const icon = {
            url: currentLocationSVG,
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
            draggable: true,
            title: "Issue's Location",
          });

          // Create a marker for each place.
          markers.push(marker);

          google.maps.event.addListener(marker, "dragend", function (evt) {
            const lat = evt.latLng.lat();
            const lng = evt.latLng.lng();

            setNewCenter(lat, lng);
            geocodeLatLng(lat, lng);
          });

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

    // Fade the background after loading
    setTimeout(() => (reduceBackGroundOpacity = true), 1500);
  });
</script>

<svelte:head>
  <title>We Move</title>
  <meta
    name="description"
    content="We Move Share the location of a walking, biking, or transit issues"
  />
</svelte:head>
{#if fadeInBackground}
  <div
    bind:this="{backgroundSelector}"
    class="background"
    class:background-opacity="{reduceBackGroundOpacity}"
    class:background-opacity-report-issue="{reportNewIssue ||
      reportNewIssueStep2 ||
      reportNewIssueStep3 ||
      reportNewIssueStep4 ||
      reportNewIssueStep5 ||
      reportNewIssueStep6}"
    in:fade="{{ duration: 3000, quintOut, amount: 10 }}"
    out:fade="{{ duration: 300, quintOut, amount: 10 }}"
  >
    <div style="display: flex; align-items: center; z-index: 1">
      {#if openLogo}
        <img
          in:scale="{{
            delay: startRendering,
            duration: 1000,
            quintOut,
          }}"
          src="{logo}"
          alt="we move logo"
          height="170rem"
          style="filter: drop-shadow(3px 3px 3px black); margin-left: 2.5rem; margin-top: 2rem"
        />
      {/if}
      {#if openWeMove}
        <div
          class="we-move"
          in:blur="{{
            delay: startRendering,
            duration: 1000,
            quintOut,
          }}"
        >
          We <span style="color: #f5b537; margin-left: 0.4rem">Move</span>
        </div>
      {/if}
    </div>
    <div
      class="content"
      in:fade="{{ delay: startRendering, duration: 1000, quintOut }}"
      out:fade="{{ duration: 300, quintOut }}"
    >
      <div class="slogan-title">Empowering Communities Together:</div>
      <div class="slogan-text">
        &nbsp;&nbsp; Share the location of a walking, biking, or transit issue
        with our mobile app
      </div>

      <div
        class="action-buttons"
        style="display: flex; justify-content: space-around"
      >
        {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5}
          <button
            class="button"
            style="margin-bottom: 1rem; background-image: radial-gradient(
      circle at 4% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    )"
            on:click="{() => {
              if (!findReportedIssue) {
                setTimeout(() => {
                  scrollToSection();
                }, 10);
                findReportedIssue = true;
                addIssuesToMap();
              } else {
                scrollToTop();
                findReportedIssue = false;
              }
            }}"
          >
            {#if !findReportedIssue}
              <img
                src="{searchSVG}"
                alt="search for reported issues"
                style="vertical-align: -0.25rem; margin-right: 1.6rem; margin-left: -0.7rem"
                height="23rem"
              />
            {:else}
              <img
                src="{closeSVG}"
                alt="close report a new issue"
                style="vertical-align: -0.3rem; margin-right: 1.3rem; margin-left: -0.7rem"
                height="25rem"
              />
            {/if}

            Find a Reported Issue
          </button>
        {/if}

        {#if !findReportedIssue}
          <button
            class="button"
            style="background-image: radial-gradient(
      circle at 6% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    )"
            on:click="{() => {
              setTimeout(() => {
                scrollToSection();
              }, 10);

              if (!reportNewIssue && !currentStep) {
                reportNewIssue = true;
                reduceBackGroundOpacity = false;
                currentStep = 1;
              } else if (reportNewIssue) {
                reportNewIssue = false;
                resetState();
              }

              if (!reportNewIssue && currentStep === 2) {
                reportNewIssueStep2 = false;
                resetState();
              }

              if (!reportNewIssue && currentStep === 3) {
                reportNewIssueStep3 = false;
                resetState();
              }

              if (!reportNewIssue && currentStep === 4) {
                reportNewIssueStep4 = false;
                resetState();
              }

              if (!reportNewIssue && currentStep === 5) {
                reportNewIssueStep5 = false;
                resetState();
              }

              // Ask for user's current location and center around it
              navigator.geolocation.getCurrentPosition(
                successCallback,
                errorCallback,
                {
                  enableHighAccuracy: true,
                }
              );
            }}"
          >
            {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5}
              <img
                src="{addSVG}"
                alt="report a new issue"
                style="vertical-align: -0.3rem; margin-right: 1.3rem; margin-left: -0.7rem"
                height="25rem"
              />
            {:else}
              <img
                src="{closeSVG}"
                alt="close report a new issue"
                style="vertical-align: -0.3rem; margin-right: 1.3rem; margin-left: -0.7rem"
                height="25rem"
              />
            {/if}
            Report a New Issue
          </button>
        {/if}
      </div>
    </div>

    <!-- START Report New Issue Flow -->

    {#if reportNewIssueStep2}
      <div style="display: flex; justify-content: center">
        <div
          id="stepIssueTypeAndDetail"
          class="describe-issue"
          style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); background-color:rgba(90,0,0,0.6); width: 42.5vw; border-radius: 21px"
          class:visible="{reportNewIssueStep2}"
          class:hidden="{!reportNewIssueStep2}"
        >
          <div
            style="margin-left: 3rem; margin-bottom: 1rem; padding-top: 1rem"
          >
            Step
            <button class="numbers">2</button>
          </div>
          <span style="margin-left: 3rem; font-size: 1.3rem">
            Date & Time: <span
              style="color: yellow; margin-left: 0.5rem; font-size: 1.3rem"
              >{$issueTime}</span
            ></span
          >

          <div
            class="describe-issue"
            style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); font-size: 1.3rem"
          >
            <span style="margin-left: 3rem">
              Select the Feature Type that you are reporting on:
            </span>
          </div>
          <div style="margin-top: -1rem">
            <select
              bind:this="{issueTypeSelector}"
              on:change="{(e) => {
                issueType.set(e.target.value);
              }}"
              style="margin-left: 3rem; margin-top: 2rem"
            >
              <option disabled selected value="">Choose an Issue Type*</option>
              <option value="Sidewalk">Sidewalk</option>
              <option value="Bike Lane">Bike Lane</option>
              <option value="Bus Stop">Bus Stop</option>
              <option value="Traffic Light">Traffic Light</option>
            </select>

            {#if $issueType === "Sidewalk"}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value=""
                  >Choose Sidewalk details*</option
                >
                <option value="ADA Access">ADA Access</option>
                <option value="Cracked">Cracked</option>
                <option value="Missing">Missing</option>
                <option value="Other">Other</option>
              </select>
            {/if}

            {#if $issueType === "Bike Lane"}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value=""
                  >Choose Bike Lane details*</option
                >
                <option value="Narrow Lanes">Narrow Lanes</option>
                <option value="Uneven Surface">Uneven Surface</option>
                <option value="Missing">Missing</option>
                <option value="Other">Other</option>
              </select>
            {/if}

            {#if $issueType === "Bus Stop"}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value=""
                  >Choose Bus Stop details*</option
                >
                <option value="Late Bus">Late Bus</option>
                <option value="Inadequate Seating">Inadequate Seating</option>
                <option value="Poor Shelter">Poor Shelter</option>
                <option value="Other">Other</option>
              </select>
            {/if}

            {#if $issueType === "Traffic Light"}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value=""
                  >Choose Traffic Light details*</option
                >
                <option value="Long Wait Time">Long Wait Time</option>
                <option value="Poor Visibility">Poor Visibility</option>
                <option value="No Audible Signal">No Audible Signal</option>
                <option value="Other">Other</option>
              </select>
            {/if}

            {#if $issueType !== null && $issueDetail !== null}
              <div>
                <textarea
                  placeholder="Additional Description Details"
                  rows="3"
                  bind:value="{$issueDescription}"></textarea>
              </div>
            {/if}
          </div>

          <button
            class="button back-button"
            style="margin-bottom: 1.25rem"
            on:click="{() => {
              reportNewIssueStep2 = false;
              reportNewIssue = true;
            }}"
          >
            <img
              src="{pageBackwardsSVG}"
              alt="previous step"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: -1rem; margin-right: 1.1rem"
            />
            Back
          </button>

          <button
            class="button"
            class:next-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            disabled="{$issueType === null || $issueDetail === null}"
            style="margin-bottom: 1.25rem"
            on:click="{() => {
              reportNewIssueStep2 = false;
              currentStep = 3;
              reportNewIssueStep3 = true;
            }}"
          >
            Next
            <img
              src="{pageForwardSVG}"
              alt="next step"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: 1rem; margin-right: -1.1rem"
            />
          </button>
        </div>
      </div>
    {/if}

    {#if reportNewIssueStep3}
      <div style="display: flex; justify-content: center">
        <div
          id="stepPhoto"
          class="describe-issue"
          style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); background-color:rgba(90,0,0,0.6); width: 45vw;border-radius: 21px"
          class:visible="{reportNewIssueStep3}"
          class:hidden="{!reportNewIssueStep3}"
        >
          <div
            style="margin-left: 3rem; margin-bottom: 1rem; padding-top: 1rem"
          >
            Step
            <button class="numbers">3</button><span style="font-size: 1.2rem"
              >(optional)</span
            >
          </div>

          <span style="font-size: 1.3rem; margin: 0 1rem 0 3rem"
            >Press Here to Take Photo or Choose Image
          </span>

          <div>
            <button class="upload-image">
              Press here to choose image file. (&lt;10MB)
            </button>

            <button>
              <!-- svelte-ignore a11y-img-redundant-alt -->
              <img
                src="{cameraSVG}"
                alt="take photo"
                width="31rem"
                style="height: 2.7rem; padding: 0 0.2rem 0 0.2rem; vertical-align:middle"
              />
            </button>

            <button
              class="button back-button"
              style="margin-bottom: 1.25rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                reportNewIssueStep2 = true;

                setTimeout(() => {
                  if ($issueType !== null) issueTypeSelector.value = $issueType;
                  if ($issueDetail !== null)
                    issueDetailSelector.value = $issueDetail;
                }, 100);
              }}"
            >
              <img
                src="{pageBackwardsSVG}"
                alt="previous step"
                width="19rem"
                style="vertical-align: -0.15rem; margin-left: -1rem; margin-right: 1.1rem"
              />
              Back
            </button>

            <button
              class="button"
              class:next-button="{$issueType && $issueDetail}"
              class:disabled-button="{$issueType === null ||
                $issueDetail === null}"
              style="margin-bottom: 1.25rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                currentStep = 4;
                reportNewIssueStep4 = true;
              }}"
            >
              Next
              <img
                src="{pageForwardSVG}"
                alt="next step"
                width="19rem"
                style="vertical-align: -0.15rem; margin-left: 1rem; margin-right: -1.1rem"
              />
            </button>
          </div>
        </div>
      </div>
    {/if}

    {#if reportNewIssueStep4}
      <div style="display: flex; justify-content: center">
        <div
          id="stepContactInfo"
          class="describe-issue"
          style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); background-color:rgba(90,0,0,0.6); width: 37vw; border-radius: 21px"
          class:visible="{reportNewIssueStep4}"
          class:hidden="{!reportNewIssueStep4}"
        >
          <div
            style="margin-left: 3rem; margin-bottom: 1rem; padding-top: 1rem"
          >
            Step
            <button class="numbers">4</button><span style="font-size: 1.2rem"
              >(optional)</span
            >
          </div>

          <span style="font-size: 1.3rem; margin: 0 1rem 0 3rem"
            >Name of Submitter:
          </span>
          <div>
            <input
              bind:value="{$issueSubmitterName}"
              style="height: 2rem; padding-left: 0.3rem; width: 25rem; margin-left: 3rem"
              placeholder="ex: John Doe"
            />
          </div>

          <span
            style="font-size: 1.3rem; margin: 0 1rem 0 3rem; text-align:left"
            >Contact Info:
          </span>
          <div>
            <input
              bind:value="{$issueSubmitterContact}"
              style="height: 2rem; padding-left: 0.3rem; width: 25rem; margin-left: 3rem"
              placeholder="ex: johndoe@gmail.com"
            />
          </div>

          <button
            class="button back-button"
            style="margin-bottom: 1.25rem"
            on:click="{() => {
              reportNewIssueStep4 = false;

              reportNewIssueStep3 = true;
            }}"
          >
            <img
              src="{pageBackwardsSVG}"
              alt="previous step"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: -1rem"
            />
            Back
          </button>

          <button
            class="button"
            class:review-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            style="margin-bottom: 1.25rem; margin-right: 3rem"
            on:click="{() => {
              reportNewIssueStep4 = false;
              currentStep = 5;
              reportNewIssueStep5 = true;
            }}"
          >
            Review & Submit
            <img
              src="{pageLastSVG}"
              alt="submit issue"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: 1rem; margin-right: -1.1rem"
            />
          </button>
        </div>
      </div>
    {/if}

    {#if reportNewIssueStep5}
      <div style="display: flex; justify-content: center">
        <div
          id="stepReviewSubmit"
          class="describe-issue"
          style="background-color:rgba(90,0,0,0.6); width: 55vw; border-radius: 21px"
          class:visible="{reportNewIssueStep5}"
          class:hidden="{!reportNewIssueStep5}"
        >
          <div
            style="margin-left: 3rem; margin-bottom: 1rem; padding-top: 1rem; text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8)"
          >
            Review
            <button class="numbers">5</button>
          </div>

          <div style="font-size: 1.5rem; margin: 0 1rem 0 3rem">
            Issue Location:
            <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
              {$issueAddress}
            </div>
          </div>
          <div style="font-size: 1.5rem; margin: 1rem 1rem 0 3rem">
            Issue Type:
            <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
              {$issueType}
            </div>
          </div>
          <div style="font-size: 1.5rem; margin: 1rem 1rem 0 3rem">
            Issue Detail:
            <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
              {$issueDetail}
            </div>
          </div>
          {#if $issueDescription}
            <div style="font-size: 1.5rem; margin: 1rem 1rem 0 3rem">
              Description:
              <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
                {$issueDescription}
              </div>
            </div>
          {/if}
          {#if $issueSubmitterName}
            <div style="font-size: 1.5rem; margin: 1rem 1rem 0 3rem">
              Submitter Name:
              <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
                {$issueSubmitterName}
              </div>
            </div>
          {/if}
          {#if $issueSubmitterContact}
            <div style="font-size: 1.5rem; margin: 1rem 1rem 0 3rem">
              Contact Info:
              <div style="font-size: 1.3rem; margin: 0.5 1rem 1rem 0">
                {$issueSubmitterContact}
              </div>
            </div>
          {/if}

          <button
            class="button back-button"
            style="margin-bottom: 1.25rem; margin-top: 2rem"
            on:click="{() => {
              reportNewIssueStep5 = false;
              reportNewIssueStep4 = true;
            }}"
          >
            <img
              src="{pageBackwardsSVG}"
              alt="previous step"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: -1rem"
            />
            Back
          </button>

          <button
            class="button"
            class:next-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            style="margin-bottom: 1.25rem; margin-right: 3rem; margin-top: 2rem"
            on:click="{() => {
              reportNewIssueStep5 = false;
              currentStep = 6;
              reportNewIssueStep6 = true;
            }}"
          >
            Submit
            <img
              src="{pageLastSVG}"
              alt="submit issue"
              width="19rem"
              style="vertical-align: -0.15rem; margin-left: 1rem; margin-right: -1.1rem"
            />
          </button>
        </div>
      </div>
    {/if}

    {#if reportNewIssueStep6}
      <div style="display: flex; justify-content: center; margin-top: 3rem">
        <div
          id="stepReviewSubmit"
          class="describe-issue"
          style="background-color:rgba(90,0,0,0.6); width: 55vw; height: 7rem; font-size: 2rem; text-align: center; border-radius: 21px"
          class:visible="{reportNewIssueStep6}"
          class:hidden="{!reportNewIssueStep6}"
        >
          <div style="margin-top: 2.3rem">
            Thank You! The issue has been reported.
          </div>
        </div>
      </div>
    {/if}

    <!-- Step 1 goes at the end because it has to be loaded due to the map and is hidden -->
    <div style="display: flex; justify-content: center; margin-top: 1rem">
      <div
        style="background-color:rgba(90,0,0,0.6); width: 55vw; border-radius: 21px"
        id="stepOne"
        class:visible="{reportNewIssue || findReportedIssue}"
        class:hidden="{!reportNewIssue && !findReportedIssue}"
      >
        {#if reportNewIssue}
          <div class="describe-issue">
            <div
              style="margin-left: 3rem; margin-bottom: 1rem; padding-top: 0.3rem"
            >
              Step
              <button class="numbers">1</button>
            </div>
            <span style="margin-left: 3rem">Where is the issue located?</span>

            <div
              style="font-size: 1.1rem; margin-top: 1.5rem; margin-left: 3rem"
            >
              Place the marker in the position where the issue occurred or type
              exact the address.
            </div>

            <div
              style="font-size: 1.3rem; margin-bottom: -1rem; margin-left: 3rem; margin-top: 2rem"
            >
              <span style="color:yellow">{$issueAddress}</span>
              <button
                class="button next-button"
                style="margin-top: 2rem; margin-bottom: 1rem; margin-right: 1rem"
                on:click="{() => {
                  reportNewIssueStep2 = true;
                  currentStep = 2;
                  reportNewIssue = false;

                  // We add delay to this assignament because of the fade in
                  setTimeout(() => {
                    if ($issueType !== null)
                      issueTypeSelector.value = $issueType;
                    if ($issueDetail !== null)
                      issueDetailSelector.value = $issueDetail;
                  }, 100);
                }}"
              >
                Next
                <img
                  src="{pageForwardSVG}"
                  alt="next step"
                  width="19rem"
                  style="vertical-align: -0.15rem; margin-left: 1rem; margin-right: -1.1rem;"
                />
              </button>
            </div>
          </div>
        {/if}

        <!-- END Report New Issue Flow -->

        <input id="pac-input" placeholder="Enter the address" type="text" />
        <div bind:this="{sectionNewReport}" id="map"></div>

        <!-- START Find Reported Issue -->

        {#if findReportedIssue}
          <div class="filters">
            <span style="color: white; font-weight: 500; font-size: 1.3rem">
              Filters
            </span>

            <select
              bind:this="{issueTypeSelectSelector}"
              on:change="{(e) => {
                filterArray = filterArray.filter(
                  (filter) => !filter.hasOwnProperty('issueType')
                );
                filterArray.push({ issueType: e.target.value });
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
              <option value="user1">User 1</option>
              <option value="user2">User 2</option>
              <option value="user3">User 3</option>
              <option value="user4">User 4</option>
            </select>

            <DateRangePicker
              on:datesSelected="{(e) => {
                filterArray = filterArray.filter(
                  (filter) => !filter.hasOwnProperty('dates')
                );
                if (e.detail.length === 2) {
                  filterArray.push({ dates: e.detail });

                  filterArray = filterArray; // Reactive statement to trigger the filter
                }
              }}"
            />
          </div>
          {#if filterArray.find((filter) => filter.hasOwnProperty("issueType"))}
            <div
              style="color: white; font-size: 0.8rem; margin-left: 2.4rem; margin-top: 1rem"
            >
              {filterArray[filterArray.findIndex((obj) => "issueType" in obj)]
                .issueType}

              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <img
                src="{closeSVG}"
                class="white-closeSVG"
                alt="remove filter"
                width="14rem"
                on:click="{() => {
                  filterArray = filterArray.filter(
                    (filter) => !filter.hasOwnProperty('issueType')
                  );
                  filteredMockData = mockData;
                  issueTypeSelectSelector.selectedIndex = 0;
                  addIssuesToMap();
                }}"
              />
            </div>
          {/if}

          {#if filterArray.find((filter) => filter.hasOwnProperty("dates"))}
            <div
              style="color: white; font-size: 0.8rem; margin-left: 2.4rem; margin-top: 1rem"
            >
              From {filterArray[filterArray.findIndex((obj) => "dates" in obj)]
                .dates[0]} to {filterArray[
                filterArray.findIndex((obj) => "dates" in obj)
              ].dates[1]}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <img
                src="{closeSVG}"
                class="white-closeSVG"
                alt="remove filter"
                width="14rem"
                on:click="{() => {
                  filterArray = filterArray.filter(
                    (filter) => !filter.hasOwnProperty('dates')
                  );
                  filteredMockData = mockData;
                  resetDate.set(true);
                  addIssuesToMap();
                }}"
              />
            </div>
          {/if}

          <div
            style="font-size: 1.5rem; font-weight: 500; color: white; margin: 1rem 0 1rem 0; text-align: center"
          >
            <hr />
            Reported Issues
          </div>

          <table class="issues-table">
            <thead>
              <tr>
                <th style="width: 14rem">Issue Type</th>
                <th style="width: 12rem">Description</th>
                <th style="width: 7rem">Media</th>
                <th style="width: 14rem">Requested At</th>
              </tr>
            </thead>

            <tbody>
              {#each filteredMockData as issue}
                <tr>
                  <td>{issue.service_name}</td>
                  <td>{issue.description}</td>
                  <td style="text-align:center">
                    {#if issue.media_url !== null}
                      <a href="{issue.media_url}" target="_blank">
                        <img
                          src="{imageSVG}"
                          alt="issue media"
                          width="15rem"
                          style="margin-right: 3.7rem"
                        />
                      </a>
                    {:else}
                      <span style="margin-right: 3.6rem">-</span>
                    {/if}
                  </td>
                  <td>{formatDate(issue.requested_datetime)}</td>
                </tr>
              {:else}
                <tr>
                  <td>No Results Found</td>
                </tr>
              {/each}
            </tbody>
          </table>
        {/if}
      </div>
    </div>
  </div>
{/if}

<style>
  .white-closeSVG {
    cursor: pointer;
    margin-left: 0.2rem;
    filter: brightness(5);
    vertical-align: sub;
    border: solid 1px white;
  }

  .background {
    width: 100vw;
    height: 100vh;
    position: relative;
    overflow: hidden;
    height: 1650px;
    background-repeat: no-repeat;
  }

  .background::before {
    content: "";
    background-image: url("$lib/streetview16-9.png");
    background-repeat: no-repeat center center fixed;
    background-position: 100% 67%;
    background-size: cover;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;
  }

  .background-opacity::before {
    filter: grayscale(70%) opacity(0.5);
    transition: all 4s;
  }

  .background-opacity-report-issue::before {
    filter: grayscale(70%) opacity(0.25);
    transition: all 4s;
  }

  .button {
    font-weight: 600;
    font-family: Raleway;
    font-size: 1.3rem;
    display: inline-block;
    padding: 10px 30px;
    color: #27279c;
    border: solid lightgray;
    border-width: 1px;
    border-radius: 25px;
    text-align: center;
    cursor: pointer;
    overflow: hidden;
    height: 3.5rem;
    box-shadow: 5px 5px 10px rgba(0, 0, 0, 0.6);
    z-index: 1;
  }

  .next-button {
    margin-top: 1.25rem;
    margin-right: 3rem;
    float: right;
    background-image: radial-gradient(
      circle at 23%,
      rgba(255, 255, 255, 1) 51%,
      rgba(190, 212, 250, 0.9) 65%,
      rgba(190, 212, 250, 0.9) 100%
    );
  }

  .review-button {
    margin-top: 1.25rem;
    margin-right: 2rem;
    float: right;
    background-image: radial-gradient(
      circle at 42%,
      rgba(255, 255, 255, 1) 60%,
      rgba(190, 212, 250, 0.9) 74%,
      rgba(190, 212, 250, 0.9) 100%
    );
  }

  .back-button {
    margin-top: 1.25rem;
    margin-left: 3rem;
    background-image: radial-gradient(
      circle at -15%,
      rgba(190, 212, 250, 0.9) 0%,
      rgba(190, 212, 250, 0.9) 35%,
      white 49%,
      white 100%
    );
  }

  .disabled-button {
    color: black;
    border: none;
    cursor: default;
    margin-top: 1.25rem;
    margin-left: 1rem;
    margin-right: 3rem;
    float: right;
    background-image: radial-gradient(
      circle at 23%,
      rgba(155, 155, 155, 1) 51%,
      rgba(80, 80, 80, 0.9) 65%,
      rgba(80, 80, 80, 0.9) 100%
    );
  }

  .slogan-title {
    margin: 7rem 0 2.5rem 8.9rem;
    font-size: 2.5rem;
    font-weight: 600;
    color: rgb(255, 255, 0);
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
  }

  .slogan-text {
    margin: 0 auto;
    font-size: 1.8rem;
    font-weight: 500;
    color: white;
    text-shadow: 2px 3px 2px rgba(0, 0, 0, 0.8);
    text-align: center;
  }

  .action-buttons {
    margin-top: 15rem;
  }

  .we-move {
    font-size: 4rem;
    font-weight: 600;
    color: white;
    text-shadow: 3px 3px 3px rgba(0, 0, 0, 0.8);
    border-radius: 25px;
    padding: 0.7rem 2rem 0.7rem 2rem;
  }

  #map {
    width: 55vw;
    height: 55vh;
    align-items: center;
  }

  .describe-issue {
    margin-top: 1rem;
    font-size: 1.75rem;
    color: white;
  }

  .numbers {
    border-radius: 100%;
    background-color: white;
    color: black;
    border: none;
    width: 1.7rem;
    height: 1.7rem;
    font-size: 1.2rem;
    vertical-align: 0.25rem;
    margin-right: 1rem;
    margin-left: 0.5rem;
  }

  #pac-input {
    margin-top: 0.6rem;
    padding-left: 0.5rem;
    height: 2.18rem;
    width: 23rem;
  }

  .hidden {
    animation: fadeOut 0.4s forwards;
  }

  .visible {
    animation: fadeIn 0.4s forwards;
  }

  select {
    height: 2.5rem;
    font-size: 1.1rem;
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    border-radius: 10px;
  }

  textarea {
    margin-top: 2rem;
    margin-left: 3rem;
    font-size: 1rem;
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    width: 80%;
  }

  .upload-image {
    margin-top: 1rem;
    margin-left: 3rem;
    width: 74.5%;
    background-color: white;
    height: 3rem;
    border-style: dashed;
  }

  .filters {
    display: flex;
    justify-content: space-around;
    width: 55vw;
    margin: 0 auto;
    padding-top: 1rem;
  }

  .issues-table {
    background-color: white;
    width: 55vw;
    max-height: 200px;
    overflow-y: auto;
    display: block;
  }

  thead tr {
    text-align: left;
    font-weight: 600;
  }

  td,
  th {
    padding: 0.1rem 0.3rem 0.1rem 0.3rem;
  }

  td {
    font-size: 0.9rem;
  }

  @keyframes fadeIn {
    0% {
      opacity: 0;
      visibility: hidden;
    }
    1% {
      visibility: visible;
    }
    100% {
      opacity: 1;
      visibility: visible;
    }
  }

  @keyframes fadeOut {
    0% {
      opacity: 1;
      visibility: visible;
    }
    99% {
      visibility: visible;
    }
    100% {
      opacity: 0;
      visibility: hidden;
    }
  }
</style>
