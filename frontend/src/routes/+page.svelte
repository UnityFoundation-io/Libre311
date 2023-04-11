<script>
  import { Loader } from "@googlemaps/js-api-loader";
  import { fade, scale, blur } from "svelte/transition";
  import { quintOut } from "svelte/easing";
  import { onMount } from "svelte";
  import { inview } from "svelte-inview";
  import axios from "axios";
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
  import detailSVG from "../icons/detail.svg";
  import issueAddress from "../stores/issueAddress";
  import issueTime from "../stores/issueTime";
  import issueType from "../stores/issueType";
  import issueDetail from "../stores/issueDetail";
  import issueDescription from "../stores/issueDescription";
  import issueSubmitterName from "../stores/issueSubmitterName";
  import issueSubmitterContact from "../stores/issueSubmitterContact";
  import userCurrentLocation from "../stores/userCurrentLocation";
  import resetDate from "../stores/resetDate";
  import {
    totalSize,
    totalPages,
    currentPage,
    itemsPerPage,
  } from "../stores/pagination";
  import DateRangePicker from "$lib/DateRangePicker.svelte";
  import Modal from "$lib/Modal.svelte";
  import messages from "$lib/messages.json";
  import colors from "$lib/colors.json";
  import "$lib/global.css";

  // Configure the backend path
  axios.defaults.baseURL = import.meta.env.VITE_BACKEND_URL;

  const hexToRGBA = (hex, alpha = 1) => {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);

    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  // Constants
  const maxCharactersLength = 4000;
  const startRendering = 2000;
  const primaryOne = colors["primary.one"];
  const primaryOneAlpha = hexToRGBA(primaryOne, 0.6);
  const primaryTwo = colors["primary.two"];
  const secondaryOne = colors["secondary.one"];
  const secondaryTwo = colors["secondary.two"];
  const accentOne = colors["accent.one"];
  const accentTwo = colors["accent.two"];
  const issueDescriptionTrimCharacters = 36;
  itemsPerPage.set(10);

  let issueTypeTrimCharacters = 20;

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
    findReportedIssue = false,
    showFilters = false,
    hasMoreResults = true,
    showModal = false;

  let backgroundSelector,
    sectionNewReport,
    map,
    geocoder,
    bounds,
    inputIssueAddress,
    issueTypeSelector,
    issueDetailSelector,
    issueTypeSelectSelector,
    selectedIssue;

  let zoom = 15;
  let markers = [];

  let issuesData = [];

  let filteredIssuesData;
  let filterArray = [];

  // Filtering Results
  $: if (filterArray.find((filter) => filter.hasOwnProperty("issueType"))) {
    filteredIssuesData = issuesData;
    const issueTypeFilter = filterArray.find((filter) =>
      filter.hasOwnProperty("issueType")
    )["issueType"];
    filteredIssuesData = filteredIssuesData.filter(
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
    filteredIssuesData = issuesData;
    filterByDates();
    addIssuesToMap();
  }

  const loader = new Loader({
    apiKey: "AIzaSyC_RuNsPOuWzMq7oiWNDxJoiqGZrOky9Kk",
    version: "weekly",
    libraries: ["places"],
  });

  $: if (reportNewIssueStep6) {
    setTimeout(() => {
      resetState();
      reportNewIssueStep6 = false;
    }, 3000);
  }

  let visibleDetails = new Set();

  const toggleDetails = (service_request_id) => {
    if (visibleDetails.has(service_request_id)) {
      visibleDetails.delete(service_request_id);
    } else {
      visibleDetails.add(service_request_id);
    }
    visibleDetails = new Set(visibleDetails);
  };

  const loadMoreResults = (e) => {
    if (e.detail.inView && hasMoreResults) {
      alert("more results loaded");
    }
  };

  const options = {
    rootMargin: "20px",
    unobserveOnEnter: true,
  };

  const getAllIssues = async () => {
    const res = await axios.get(
      `http://localhost:8080/api/requests?size=${$itemsPerPage}`
    );
    console.log("res", res);
    if (res) {
      issuesData = res.data;
      filteredIssuesData = issuesData;
      totalSize.set(res.headers["page-totalsize"]);
      totalPages.set(res.headers["page-totalpages"]);
      currentPage.set(res.headers["page-pagenumber"]);
    }
  };

  const filterByDates = () => {
    const selectedDates = filterArray.find((filter) =>
      filter.hasOwnProperty("dates")
    )["dates"];
    const filterInitialDate = new Date(selectedDates[0]);
    const filterEndingDate = new Date(selectedDates[1]);
    filteredIssuesData = filteredIssuesData.filter(
      (issue) =>
        new Date(issue.requested_datetime) > filterInitialDate &&
        new Date(issue.requested_datetime) < filterEndingDate
    );
  };

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const scrollToSection = (value) => {
    const y =
      sectionNewReport.getBoundingClientRect().top + window.pageYOffset + value;
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
          console.log(messages["geocode"]["empty.results"]);
        }
      } else {
        console.log(`${messages["geocode"]["error"]} ${status}`);
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
    issueSubmitterContact.set();
    issueSubmitterName.set();
    $issueDetail = null;
    $issueType = null;
    inputIssueAddress.value = "";
    setTimeout(() => (currentStep = null), 700);
  };

  const formatRelativeDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);

    const days = Math.floor(diffInSeconds / 86400);
    const hours = Math.floor((diffInSeconds % 86400) / 3600);
    const minutes = Math.floor((diffInSeconds % 3600) / 60);
    const seconds = diffInSeconds % 60;

    let formattedDate = "";
    if (days > 0) {
      formattedDate = `${days} day${days > 1 ? "s" : ""} ago`;
    } else {
      if (hours > 0) {
        formattedDate += `${hours}h `;
      }
      if (hours === 0 && minutes > 0) {
        formattedDate += `${minutes}m `;
      }
      if (hours === 0 && minutes === 0 && seconds > 0) {
        formattedDate += `${seconds}s `;
      }
      formattedDate += "ago";
    }
    return formattedDate.trim();
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
    } ${day}, ${year} ${hours}:${minutes.toString().padStart(2, "0")}`;
    return formattedDate;
  };

  const addIssuesToMap = async () => {
    clearMarkers();
    if (filteredIssuesData && filteredIssuesData.length > 0) {
      filteredIssuesData.forEach((issue) => {
        let marker, urlIcon;

        marker = new google.maps.Marker({
          position: {
            lat: parseFloat(issue.lat),
            lng: parseFloat(issue.long),
          },
          map: map,
          title: issue.name,
        });
        markers.push(marker);
      });

      setTimeout(() => {
        calculateBoundsAroundMarkers();
      }, 400);
    }
  };

  const calculateBoundsAroundMarkers = () => {
    if (markers && bounds) {
      let lat, lng;
      bounds = new google.maps.LatLngBounds();
      for (let i = 0; i < markers.length; i++) {
        lat = markers[i].position.lat();
        lng = markers[i].position.lng();
        bounds.extend({ lat, lng });
      }
      setNewCenter(bounds.getCenter());
      map.fitBounds(bounds);
    }
  };

  const loadColorPalette = () => {
    const colorStyle = document.createElement("style");
    colorStyle.textContent = `
      :root {
          --primary-color-one: ${primaryOne};
          --primary-color-one-alpha: ${primaryOneAlpha};
          --primary-color-two: ${primaryTwo};
          --secondary-color-one: ${secondaryOne};
          --secondary-color-two: ${secondaryTwo};
          --accent-color-one: ${accentOne};
          --accent-color-two: ${accentTwo};
        }
    `;

    document.head.appendChild(colorStyle);
  };

  const handleBeforeUnload = (event) => {
    const message =
      "Are you sure you want to leave? Your unsaved changes will be lost.";
    event.preventDefault();
    event.returnValue = message;
    return message;
  };

  const openInNewWindow = (url) => {
    window.open(url, "_blank");
  };

  onMount(async () => {
    // We shorten the Issue Type number of characters for small screens
    if (window.innerWidth < 677) issueTypeTrimCharacters = 15;

    // Warn user before leaving the website
    window.addEventListener("beforeunload", handleBeforeUnload);

    loadColorPalette();

    scrollToTop();

    await getAllIssues();

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
        position: map.getCenter(),
        title: messages["map"]["marker.title"],
      });

      map.addListener("center_changed", async () => {
        const center = map.getCenter();
        marker.setPosition(center);
        geocodeLatLng(center.lat(), center.lng());
      });

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
            console.log(messages["map"]["empty.geometry"]);
            return;
          }

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
  <title>{messages["home"]["title.header"]}</title>
  <meta name="description" content="{messages['metadata']['content']}" />
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
          class="logo"
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
          {messages["home"]["title.one"]}
          <span style="color: {primaryTwo}; margin-left: 0.4rem">
            {messages["home"]["title.two"]}
          </span>
        </div>
      {/if}
    </div>

    <div
      class="content"
      in:fade="{{ delay: startRendering, duration: 1000, quintOut }}"
      out:fade="{{ duration: 300, quintOut }}"
    >
      <div class="slogan-title">{messages["home"]["tagline.one"]}</div>
      <div class="slogan-text">
        &nbsp;&nbsp; {messages["home"]["tagline.two"]}
      </div>

      <div class="action-buttons">
        {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5}
          <button
            class="button"
            class:button-find-issue-disabled="{showModal}"
            disabled="{showModal}"
            id="button-find-issues"
            on:click="{() => {
              if (reportNewIssueStep6) return;

              if (!findReportedIssue) {
                setTimeout(() => {
                  scrollToSection(-80);
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
            {messages["home"]["button.find.issue.label"]}
          </button>
        {/if}

        {#if !findReportedIssue}
          <button
            class="button"
            id="button-report-issue"
            on:click="{() => {
              if (reportNewIssueStep6) return;

              setTimeout(() => {
                scrollToSection(-380);
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
                class="add-svg"
                height="25rem"
              />
            {:else}
              <img
                src="{closeSVG}"
                alt="close report a new issue"
                style="vertical-align: -0.3rem; margin-right: 1.3rem; margin-left: -2.1rem"
                height="25rem"
              />
            {/if}
            {messages["home"]["button.report.issue.label"]}
          </button>
        {/if}
      </div>
    </div>

    <!-- START Report New Issue Flow -->

    {#if reportNewIssueStep2}
      <div class="step-two-div">
        <div
          id="stepIssueTypeAndDetail"
          class="describe-issue-two"
          class:visible="{reportNewIssueStep2}"
          class:hidden="{!reportNewIssueStep2}"
        >
          <div class="step-two-label">
            {messages["report.issue"]["label.step"]}
            <button class="numbers">2</button>
          </div>
          <span class="step-two-date-label">
            {messages["report.issue"]["label.date"]}
            <span class="step-two-date-timestamp">
              {$issueTime}
            </span>
          </span>
          <div
            class="describe-issue"
            style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); font-size: 1.3rem"
          >
            <span class="step-two-feature-type-label">
              {messages["report.issue"]["label.feature.type"]}
            </span>
          </div>
          <div class="step-two-select-div">
            <select
              class="step-two-select"
              bind:this="{issueTypeSelector}"
              on:change="{(e) => {
                issueType.set(e.target.value);
              }}"
            >
              <option disabled selected value="">
                {messages["report.issue"][
                  "select.option.issue.type.placeholder"
                ]}
              </option>
              <option
                value="{messages['report.issue'][
                  'select.option.issue.type.one'
                ]}"
              >
                {messages["report.issue"]["select.option.issue.type.one"]}
              </option>
              <option
                value="{messages['report.issue'][
                  'select.option.issue.type.two'
                ]}"
              >
                {messages["report.issue"]["select.option.issue.type.two"]}
              </option>
              <option
                value="{messages['report.issue'][
                  'select.option.issue.type.three'
                ]}"
              >
                {messages["report.issue"]["select.option.issue.type.three"]}
              </option>
              <option
                value="{messages['report.issue'][
                  'select.option.issue.type.four'
                ]}"
              >
                {messages["report.issue"]["select.option.issue.type.four"]}
              </option>
            </select>

            {#if $issueType === messages["report.issue"]["select.option.issue.type.one"]}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value="">
                  {messages["issue.one"]["select.option.placeholder"]}
                </option>
                <option value="{messages['issue.one']['select.option.one']}">
                  {messages["issue.one"]["select.option.one"]}
                </option>
                <option value="{messages['issue.one']['select.option.two']}">
                  {messages["issue.one"]["select.option.two"]}
                </option>
                <option value="{messages['issue.one']['select.option.three']}">
                  {messages["issue.one"]["select.option.three"]}
                </option>
                <option value="{messages['issue.one']['select.option.four']}">
                  {messages["issue.one"]["select.option.four"]}
                </option>
              </select>
            {/if}

            {#if $issueType === messages["report.issue"]["select.option.issue.type.two"]}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value="">
                  {messages["issue.two"]["select.option.placeholder"]}
                </option>
                <option value="{messages['issue.two']['select.option.one']}">
                  {messages["issue.two"]["select.option.one"]}
                </option>
                <option value="{messages['issue.two']['select.option.two']}">
                  {messages["issue.two"]["select.option.two"]}
                </option>
                <option value="{messages['issue.two']['select.option.three']}">
                  {messages["issue.two"]["select.option.three"]}
                </option>
                <option value="{messages['issue.two']['select.option.four']}">
                  {messages["issue.two"]["select.option.four"]}
                </option>
              </select>
            {/if}
            {#if $issueType === messages["report.issue"]["select.option.issue.type.three"]}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value="">
                  {messages["issue.three"]["select.option.placeholder"]}
                </option>
                <option value="{messages['issue.three']['select.option.one']}">
                  {messages["issue.three"]["select.option.one"]}
                </option>
                <option value="{messages['issue.three']['select.option.two']}">
                  {messages["issue.three"]["select.option.two"]}
                </option>
                <option
                  value="{messages['issue.three']['select.option.three']}"
                >
                  {messages["issue.three"]["select.option.three"]}
                </option>
                <option value="{messages['issue.three']['select.option.four']}">
                  {messages["issue.three"]["select.option.four"]}
                </option>
              </select>
            {/if}

            {#if $issueType === messages["report.issue"]["select.option.issue.type.four"]}
              <select
                bind:this="{issueDetailSelector}"
                style="margin-left: 1rem; margin-top: 2rem"
                on:change="{(e) => {
                  issueDetail.set(e.target.value);
                }}"
              >
                <option disabled selected value="">
                  {messages["issue.four"]["select.option.placeholder"]}
                </option>
                <option value="{messages['issue.four']['select.option.one']}">
                  {messages["issue.four"]["select.option.one"]}
                </option>
                <option value="{messages['issue.four']['select.option.two']}">
                  {messages["issue.four"]["select.option.two"]}
                </option>
                <option value="{messages['issue.four']['select.option.three']}">
                  {messages["issue.four"]["select.option.three"]}
                </option>
                <option value="{messages['issue.four']['select.option.four']}">
                  {messages["issue.four"]["select.option.four"]}
                </option>
              </select>
            {/if}
            {#if $issueType !== null && $issueDetail !== null}
              <div>
                <textarea
                  placeholder="{messages['report.issue'][
                    'textarea.description.placeholder'
                  ]}"
                  rows="3"
                  maxlength="{maxCharactersLength}"
                  bind:value="{$issueDescription}"></textarea>
              </div>

              <div class="step-two-word-count">
                {$issueDescription?.length ?? 0}/{maxCharactersLength}
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
            {messages["report.issue"]["button.back"]}
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
            {messages["report.issue"]["button.next"]}
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
          class:visible="{reportNewIssueStep3}"
          class:hidden="{!reportNewIssueStep3}"
        >
          <div class="step-three-label">
            {messages["report.issue"]["label.step"]}
            <button class="numbers">3</button>
            <span style="font-size: 1.2rem">
              {messages["report.issue"]["label.optional"]}
            </span>
          </div>
          <span class="step-three-add-media-label">
            {messages["report.issue"]["label.add.media"]}
          </span>
          <div>
            <button class="upload-image">
              {messages["report.issue"]["lable.choose.image"]}
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
              style="margin-bottom: 1.25rem; margin-top: 2rem"
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
              {messages["report.issue"]["button.back"]}
            </button>

            <button
              class="button"
              class:next-button="{$issueType && $issueDetail}"
              class:disabled-button="{$issueType === null ||
                $issueDetail === null}"
              style="margin-bottom: 1.25rem; margin-top: 2rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                currentStep = 4;
                reportNewIssueStep4 = true;
              }}"
            >
              {messages["report.issue"]["button.next"]}

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
          class:visible="{reportNewIssueStep4}"
          class:hidden="{!reportNewIssueStep4}"
        >
          <div class="step-four-label">
            {messages["report.issue"]["label.step"]}
            <button class="numbers">4</button>
            <span style="font-size: 1.2rem">
              {messages["report.issue"]["label.optional"]}
            </span>
          </div>

          <div>
            <span class="step-four-submitter-name-label">
              {messages["report.issue"]["label.submitter.name"]}
            </span>
            <input
              class="step-four-input-submitter-name"
              bind:value="{$issueSubmitterName}"
              placeholder="{messages['report.issue'][
                'placeholder.submitter.name'
              ]}"
            />
          </div>

          <div>
            <span class="step-four-contact-info-label">
              {messages["report.issue"]["label.contact.info"]}
            </span>
            <input
              class="step-four-input-contact-info"
              bind:value="{$issueSubmitterContact}"
              placeholder="{messages['report.issue'][
                'placeholder.contact.info'
              ]}"
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
              style="vertical-align: -0.15rem; margin-left: -1rem; margin-right: 1rem"
            />
            {messages["report.issue"]["button.back"]}
          </button>

          <button
            class="button review-submit"
            class:review-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            on:click="{() => {
              reportNewIssueStep4 = false;
              currentStep = 5;
              reportNewIssueStep5 = true;
            }}"
          >
            {messages["report.issue"]["button.review.submit"]}
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
          style=""
          class:visible="{reportNewIssueStep5}"
          class:hidden="{!reportNewIssueStep5}"
        >
          <div class="step-five-label">
            {messages["report.issue"]["label.review.title"]}
            <button class="numbers">5</button>
          </div>
          <div class="step-five-issue-location-label">
            {messages["report.issue"]["label.review.issue.location"]}
            <div class="step-five-issue-location-address">
              {$issueAddress}
            </div>
          </div>
          <div class="step-five-issue-type-label">
            {messages["report.issue"]["label.review.issue.type"]}
            <div class="step-five-issue-type">
              {$issueType}
            </div>
          </div>
          <div class="step-five-issue-detail-label">
            {messages["report.issue"]["label.review.issue.detail"]}
            <div class="step-five-issue-detail">
              {$issueDetail}
            </div>
          </div>
          {#if $issueDescription}
            <div class="step-five-issue-description-label">
              {messages["report.issue"]["label.review.description"]}
              <div class="step-five-issue-description">
                {#if $issueDescription.length > issueDescriptionTrimCharacters}
                  {$issueDescription.slice(
                    0,
                    issueDescriptionTrimCharacters
                  )}...
                {:else}
                  {$issueDescription}
                {/if}
              </div>
            </div>
          {/if}
          {#if $issueSubmitterName}
            <div class="step-five-submitter-name-label">
              {messages["report.issue"]["label.submitter.name"]}
              <div class="step-five-submitter-name">
                {$issueSubmitterName}
              </div>
            </div>
          {/if}
          {#if $issueSubmitterContact}
            <div class="step-five-contact-info-label">
              {messages["report.issue"]["label.contact.info"]}
              <div class="step-five-contact-info">
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
              style="vertical-align: -0.15rem; margin-left: -1rem; margin-right: 1rem"
            />
            {messages["report.issue"]["button.back"]}
          </button>
          <button
            class="button submit-button"
            class:next-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            on:click="{() => {
              reportNewIssueStep5 = false;
              currentStep = 6;
              reportNewIssueStep6 = true;
            }}"
          >
            {messages["report.issue"]["button.submit"]}
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
          class="describe-issue success-message-div"
          class:visible="{reportNewIssueStep6}"
          class:hidden="{!reportNewIssueStep6}"
        >
          <div class="success-message">
            {messages["report.issue"]["issue.reported.success.message.one"]}
          </div>
          <div class="success-message-two">
            {messages["report.issue"]["issue.reported.success.message.two"]}
          </div>
        </div>
      </div>
    {/if}

    <!-- Step 1 goes at the end because it has to be loaded due to the map and is hidden -->

    <div class="find-issues">
      <div
        id="stepOne"
        class:visible="{reportNewIssue || findReportedIssue}"
        class:hidden="{!reportNewIssue && !findReportedIssue}"
      >
        {#if reportNewIssue}
          <div class="describe-issue">
            <div class="step-one-label">
              {messages["report.issue"]["label.step"]}
              <button class="numbers">1</button>
            </div>
            <div class="step-one-issue-location-label">
              {messages["report.issue"]["label.issue.location"]}
            </div>
            <div class="step-one-instruction">
              {messages["report.issue"]["label.issue.location.subtext"]}
            </div>
            <div class="step-one-issue-address">
              <span style="color: {primaryTwo}">{$issueAddress}</span>
              <button
                class="button next-button step-one-button-next"
                on:click="{() => {
                  reportNewIssueStep2 = true;
                  currentStep = 2;
                  reportNewIssue = false;

                  // We add delay to support the fade in
                  setTimeout(() => {
                    if ($issueType !== null)
                      issueTypeSelector.value = $issueType;
                    if ($issueDetail !== null)
                      issueDetailSelector.value = $issueDetail;
                  }, 100);
                }}"
              >
                {messages["report.issue"]["button.next"]}

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

        {#if showModal}
          <Modal
            title="Issue Details"
            selectedIssue="{selectedIssue}"
            color="{primaryOne}"
            on:cancel="{() => (showModal = false)}"
          >
            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem">Type:</span>
              {selectedIssue.service_name}
            </div>
            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >Description:</span
              >{selectedIssue.description}
            </div>
            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >Requested At:</span
              >{formatDate(selectedIssue.requested_datetime)}
            </div>
            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >Location:</span
              >{$issueAddress}
            </div>
            {#if selectedIssue.media_url !== undefined}
              <!-- svelte-ignore a11y-img-redundant-alt -->
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <img
                src="{selectedIssue.media_url}"
                alt="photo of the issue"
                width="175px"
                style="margin-top: 0.5rem; border-radius: 15px; box-shadow: 4px 4px 4px rgba(0, 0, 0, 0.4)"
                on:click="{() => openInNewWindow(selectedIssue.media_url)}"
              />
            {/if}
          </Modal>
        {/if}

        {#if findReportedIssue}
          <div class="filter-label">
            {messages["find.issue"]["label.filter"]}
            {#if showFilters}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <span on:click="{() => (showFilters = !showFilters)}">-</span>
            {:else}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <span on:click="{() => (showFilters = !showFilters)}">+</span>
            {/if}
          </div>

          {#if showFilters}
            <div class="filters">
              <select
                bind:this="{issueTypeSelectSelector}"
                on:change="{(e) => {
                  filterArray = filterArray.filter(
                    (filter) => !filter.hasOwnProperty('issueType')
                  );
                  filterArray.push({ issueType: e.target.value });
                }}"
              >
                <option disabled selected value="">
                  {messages["find.issue"]["issue.type.placeholder"]}
                </option>
                <option
                  value="{messages['find.issue'][
                    'select.option.issue.type.one'
                  ]}"
                >
                  {messages["find.issue"][
                    "select.option.issue.type.one"
                  ]}</option
                >
                <option
                  value="{messages['find.issue'][
                    'select.option.issue.type.two'
                  ]}"
                >
                  {messages["find.issue"]["select.option.issue.type.two"]}
                </option>
                <option
                  value="{messages['find.issue'][
                    'select.option.issue.type.three'
                  ]}"
                >
                  {messages["find.issue"]["select.option.issue.type.three"]}
                </option>
                <option
                  value="{messages['find.issue'][
                    'select.option.issue.type.four'
                  ]}"
                >
                  {messages["find.issue"]["select.option.issue.type.four"]}
                </option>
              </select>

              <select
                on:change="{(e) => {
                  console.log(e.target.value);
                }}"
              >
                <option disabled selected value="">
                  {messages["find.issue"]["reported.by.placeholder"]}
                </option>
                <option value="user1">
                  {messages["find.issue"]["select.option.reported.by.one"]}
                </option>
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
              {#if filterArray.find( (filter) => filter.hasOwnProperty("issueType") )}
                <div
                  style="color: white; font-size: 0.8rem; margin-left: 2.4rem; margin-top: 1rem"
                >
                  {filterArray[
                    filterArray.findIndex((obj) => "issueType" in obj)
                  ].issueType}
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
                      filteredIssuesData = issuesData;
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
                  From {filterArray[
                    filterArray.findIndex((obj) => "dates" in obj)
                  ].dates[0]} to {filterArray[
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
                      filteredIssuesData = issuesData;
                      resetDate.set(true);
                      addIssuesToMap();
                    }}"
                  />
                </div>
              {/if}
            </div>
          {/if}

          <div class="reported-issues-label">
            <!-- <hr /> -->
            {messages["find.issue"]["label.reported.issues"]}
          </div>
          <table class="issues-table">
            <thead>
              <tr>
                <th>
                  {messages["find.issue"]["issues.table.column.one"]}
                </th>
                <th>
                  {messages["find.issue"]["issues.table.column.two"]}
                </th>
                <th>
                  {messages["find.issue"]["issues.table.column.three"]}
                </th>
                <th style="width: 14rem; text-align: center">
                  {messages["find.issue"]["issues.table.column.four"]}
                </th>
              </tr>
            </thead>

            <tbody>
              {#each filteredIssuesData as issue (issue.service_request_id)}
                <tr>
                  <!-- svelte-ignore a11y-click-events-have-key-events -->
                  <td
                    on:click="{() => {
                      toggleDetails(issue.service_request_id);
                      selectedIssue = issue;
                    }}"
                  >
                    {#if issue.service_name.length > issueTypeTrimCharacters}
                      {issue.service_name.slice(0, issueTypeTrimCharacters)}...
                    {:else}
                      {issue.service_name}
                    {/if}
                  </td>
                  <!-- svelte-ignore a11y-click-events-have-key-events -->
                  <td
                    class="td-description"
                    on:click="{() => {
                      toggleDetails(issue.service_request_id);
                      selectedIssue = issue;
                    }}">{issue.description}</td
                  >
                  <td style="text-align: center">
                    {#if issue.media_url !== undefined}
                      <!-- svelte-ignore a11y-click-events-have-key-events -->
                      <img
                        src="{imageSVG}"
                        alt="issue media"
                        width="15rem"
                        style="margin-right: 0 auto; cursor: pointer; text-align: center"
                        on:click="{() => openInNewWindow(issue.media_url)}"
                      />
                    {:else}
                      <span style="text-align: center">-</span>
                    {/if}
                  </td>
                  <!-- svelte-ignore a11y-click-events-have-key-events -->
                  <td
                    style="text-align: center"
                    on:click="{() => {
                      toggleDetails(issue.service_request_id);
                      selectedIssue = issue;
                    }}"
                  >
                    {formatRelativeDate(issue.requested_datetime)}
                  </td>
                </tr>

                {#if visibleDetails.has(issue.service_request_id)}
                  <tr style="background-color: {hexToRGBA(secondaryTwo, 0.1)}">
                    <td class="issue-detail-view">{issue.service_name}</td>
                    <td class="issue-detail-view">{issue.description}</td>
                    <td style="text-align: center">
                      <!-- svelte-ignore a11y-click-events-have-key-events -->
                      <img
                        src="{detailSVG}"
                        alt="detail view"
                        height="17rem"
                        on:click="{() => {
                          showModal = true;
                          geocodeLatLng(selectedIssue.lat, selectedIssue.long);
                        }}"
                      />
                    </td>

                    <td></td>
                  </tr>{/if}
              {:else}
                <tr>
                  <td>{messages["find.issue"]["empty.results"]}</td>
                </tr>
              {/each}
              <div
                use:inview="{{ options }}"
                on:change="{loadMoreResults}"
              ></div>
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
    width: 100%;
    height: 100%;
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
      circle at 37%,
      rgba(255, 255, 255, 1) 64%,
      rgba(190, 212, 250, 0.9) 78%,
      rgba(190, 212, 250, 0.9) 100%
    );
  }

  .review-submit {
    margin-bottom: 1.25rem;
    margin-right: 3rem;
  }

  .submit-button {
    margin-bottom: 1.25rem;
    margin-right: 3rem;
    margin-top: 2rem;
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

  .button-find-issue-disabled {
    filter: grayscale(100%);
    cursor: default;
  }

  .slogan-title {
    margin: 7rem 0 2.5rem 7.9rem;
    font-size: 2.5rem;
    font-weight: 600;
    color: var(--primary-color-two);
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

  .add-svg {
    vertical-align: -0.3rem;
    margin-right: 1.3rem;
    margin-left: -2.1rem;
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
    font-weight: 200;
    margin-top: 1rem;
    font-size: 1.75rem;
    color: white;
  }

  .describe-issue-two {
    font-weight: 200;
    margin-top: 1rem;
    font-size: 1.75rem;
    color: white;
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
    background-color: var(--primary-color-one);
    width: 42.5vw;
    border-radius: 21px;
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
    margin-bottom: 1rem;
    font-size: 1rem;
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    width: 80%;
  }

  textarea::placeholder {
    font-size: 0.85rem;
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
    padding-top: 0.5rem;
  }

  .filter-label {
    color: white;
    font-weight: 300;
    font-size: 1.1rem;
    margin: 0.5rem 0 0 1rem;
  }

  .issues-table {
    background-color: white;
    width: 55vw;
    max-height: 15rem;
    overflow-y: auto;
    display: block;
  }

  .issue-detail-view {
    overflow: unset;
    text-overflow: ellipsis;
    white-space: unset;
    font-size: 0.8rem;
  }

  .issue-detail-line {
    margin-bottom: 0.75rem;
    font-weight: 100;
    font-size: 1.2rem;
  }

  thead tr {
    text-align: left;
    font-weight: 600;
  }

  tbody tr {
    text-align: left;
    font-weight: 100;
  }

  td,
  th {
    padding: 0.1rem 0.3rem 0.1rem 0.3rem;
    white-space: nowrap;
  }

  td {
    font-size: 0.9rem;
    max-width: 24rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    cursor: pointer;
  }

  .td-description {
    max-width: 22rem;
  }

  .find-issues {
    display: flex;
    justify-content: center;
    margin-top: 1rem;
  }

  .logo {
    filter: drop-shadow(3px 3px 3px black);
    margin-left: 2.5rem;
    margin-top: 2rem;
    height: 10rem;
  }

  .action-buttons {
    display: flex;
    justify-content: space-around;
  }

  .reported-issues-label {
    font-size: 1.5rem;
    font-weight: 500;
    color: white;
    margin: 0.5rem 0 0.5rem 0;
    text-align: center;
  }

  #stepOne {
    background-color: var(--primary-color-one);
    width: 55vw;
    border-radius: 21px;
  }

  .step-one-issue-location-label {
    margin-left: 3rem;
  }

  .step-one-label {
    margin-left: 3rem;
    margin-bottom: 1rem;
    padding-top: 0.3rem;
    font-weight: 300;
  }

  .step-one-instruction {
    font-size: 1rem;
    margin-top: 1.5rem;
    margin-left: 3rem;
  }

  .step-one-issue-address {
    font-size: 1.3rem;
    margin-bottom: -1rem;
    margin-left: 3rem;
    margin-top: 2rem;
  }

  .step-one-button-next {
    margin-top: 2rem;
    margin-bottom: 1rem;
    margin-right: 1rem;
  }

  .step-two-label {
    margin-left: 3rem;
    margin-bottom: 1rem;
    padding-top: 1rem;
    min-width: 90vw;
    font-weight: 300;
  }

  .step-two-div {
    display: flex;
    justify-content: center;
  }

  .step-two-date-label {
    margin-left: 3rem;
    font-size: 1.3rem;
  }

  .step-two-date-timestamp {
    color: var(--primary-color-two);
    margin-left: 0.5rem;
    font-size: 1.3rem;
  }

  .step-two-feature-type-label {
    margin-left: 3rem;
    font-size: 1.2rem;
  }

  .step-two-select-div {
    margin-top: -1rem;
  }

  .step-two-select {
    margin-left: 3rem;
    margin-top: 2rem;
    margin-bottom: 1rem;
  }

  .step-two-word-count {
    font-size: 0.75rem;
    text-align: right;
    margin-top: 0.2rem;
    margin-right: 3.3rem;
  }

  #stepPhoto {
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
    background-color: var(--primary-color-one);
    width: 45vw;
    border-radius: 21px;
  }

  .step-three-label {
    margin-left: 3rem;
    margin-bottom: 1rem;
    padding-top: 1rem;
    font-weight: 300;
  }

  .step-three-add-media-label {
    font-size: 1.3rem;
    margin: 0 1rem 0 3rem;
  }

  #stepContactInfo {
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
    background-color: var(--primary-color-one);
    width: 37vw;
    border-radius: 21px;
  }

  .step-four-label {
    margin-left: 3rem;
    margin-bottom: 1rem;
    padding-top: 1rem;
    font-weight: 300;
  }

  .step-four-submitter-name-label {
    font-size: 1.3rem;
    margin: 0 1rem 0 3rem;
  }

  .step-four-input-submitter-name {
    height: 2rem;
    padding-left: 0.3rem;
    width: 25rem;
    margin-left: 3rem;
    margin-bottom: 1rem;
  }

  .step-four-contact-info-label {
    font-size: 1.3rem;
    margin: 0 1rem 0 3rem;
    text-align: left;
  }

  .step-four-input-contact-info {
    height: 2rem;
    padding-left: 0.3rem;
    width: 25rem;
    margin-left: 3rem;
    margin-bottom: 1rem;
  }

  .step-four-input-contact-info::placeholder {
    font-size: 0.85rem;
  }

  #stepReviewSubmit {
    background-color: var(--primary-color-one);
    width: 55vw;
    height: fit-content;
    border-radius: 21px;
  }

  .step-five-label {
    margin-left: 3rem;
    margin-bottom: 0.3rem;
    padding-top: 1rem;
    text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8);
    font-weight: 300;
  }

  .step-five-issue-location-label {
    font-size: 1.5rem;
    margin: 0 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-issue-location-address {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .step-five-issue-type-label {
    font-size: 1.5rem;
    margin: 1rem 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-issue-type {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .step-five-issue-detail-label {
    font-size: 1.5rem;
    margin: 1rem 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-issue-detail {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .step-five-issue-description-label {
    font-size: 1.5rem;
    margin: 1rem 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-issue-description {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .step-five-submitter-name-label {
    font-size: 1.5rem;
    margin: 1rem 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-submitter-name {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .step-five-contact-info-label {
    font-size: 1.5rem;
    margin: 1rem 1rem 0 3rem;
    font-weight: 300;
  }

  .step-five-contact-info {
    font-size: 1.3rem;
    margin: 0.5 1rem 1rem 0;
    font-weight: 100;
  }

  .success-message-div {
    background-color: var(--primary-color-one);
    width: 55vw;
    height: 7rem;
    font-size: 2rem;
    text-align: center;
    border-radius: 21px;
  }

  .success-message {
    margin-top: 1.3rem;
  }

  .success-message-two {
    margin-top: 1.3rem;
    margin-bottom: 1.3rem;
  }

  #button-find-issues {
    width: 20rem;

    background-image: radial-gradient(
      circle at 4% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    );
  }

  #button-report-issue {
    width: 20rem;

    background-image: radial-gradient(
      circle at 6% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    );
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

  /* Styles for screen widths between 375px and 844px */
  @media only screen and (min-width: 375px) and (max-width: 844px) {
    .content {
      font-size: 0.5rem;
    }

    .issues-table {
      background-color: white;
      width: 100vw;
      max-height: 13rem;
      overflow-y: auto;
      display: block;
      font-size: 0.8rem;
    }

    .issue-detail-view {
      font-size: 0.65rem;
    }

    .issue-detail-line {
      font-size: 0.85rem;
    }

    td {
      font-size: 0.7rem;
    }

    .td-description {
      max-width: 6.5rem;
    }

    .find-issues {
      justify-content: left;
    }

    #stepOne {
      width: 100vw;
    }

    .step-one-issue-location-label {
      margin-top: -1rem;
      margin-left: 0.5rem;
      font-size: 1.5rem;
    }

    .step-one-label {
      margin-left: 0.5rem;
    }

    .step-one-instruction {
      margin-top: 0;
      margin-left: 0.5rem;
    }

    .step-one-issue-address {
      font-size: 1rem;
      font-weight: 300;
      margin-top: 0.5rem;
      margin-left: 0.5rem;
      filter: brightness(120%);
    }

    .step-one-button-next {
      margin-right: 0;
    }

    .step-two-label {
      margin-left: 0.5rem;
    }

    .step-two-date-label {
      font-size: 1.2rem;
      margin-left: 0.5rem;
    }

    .step-two-date-timestamp {
      font-size: 1.1rem;
      margin-left: 0rem;
      filter: brightness(120%);
    }

    .step-two-feature-type-label {
      font-size: 0.97rem;
      letter-spacing: -0.03rem;
      margin-left: 0.5rem;
    }

    .step-two-select-div {
      text-align: center;
    }

    .step-two-select {
      margin-left: 0;
      text-align: center;
    }

    .step-two-word-count {
      margin-top: 0;
      margin-right: 1rem;
      margin-bottom: 1rem;
    }

    #stepPhoto {
      width: 100vw;
    }

    .step-three-label {
      margin-left: 0.5rem;
    }

    .step-three-add-media-label {
      font-size: 1.045rem;
      margin: 0 1rem 0 0.5rem;
    }

    #stepContactInfo {
      width: 100vw;
    }

    .step-four-label {
      margin-left: 0.5rem;
    }

    .step-four-submitter-name-label {
      font-size: 1.3rem;
      margin: 0 1rem 0 0.5rem;
    }

    .step-four-input-submitter-name {
      margin-left: 0.5rem;
      width: 93vw;
    }

    .step-four-contact-info-label {
      margin-left: 0.5rem;
      font-size: 1.3rem;
    }

    .step-four-input-contact-info {
      margin-left: 0.5rem;
      width: 93vw;
    }

    #stepReviewSubmit {
      width: 100vw;
    }

    .step-five-label {
      margin-left: 0.5rem;
    }

    .step-five-issue-location-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-issue-location-address {
      font-size: 1rem;
    }

    .step-five-issue-type-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-issue-type {
      font-size: 1rem;
    }

    .step-five-issue-detail-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-issue-detail {
      font-size: 1rem;
    }

    .step-five-issue-description-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-issue-description {
      font-size: 1rem;
    }

    .step-five-submitter-name-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-submitter-name {
      font-size: 1rem;
    }

    .step-five-contact-info-label {
      margin-left: 0.5rem;
      font-size: 1.1rem;
    }

    .step-five-contact-info {
      font-size: 1rem;
    }

    .success-message-div {
      width: 100vw;
      font-size: 1.5rem;
    }

    .success-message {
      margin-top: 1rem;
    }

    .upload-image {
      margin-left: 0.5rem;
    }

    .describe-issue {
      width: 100vw;
    }

    .describe-issue-two {
      width: 100vw;
    }

    .next-button {
      margin-right: 0.5rem;
    }

    .back-button {
      margin-left: 0.5rem;
    }

    .review-button {
      margin-right: 0.5rem;
    }

    .review-submit {
      margin-right: 0.5rem;
    }

    .submit-button {
      margin-right: 0.5rem;
    }

    .disabled-button {
      margin-right: 0.5rem;
    }

    #map {
      width: 100vw;
      height: 43vh;
    }

    #pac-input {
      width: 5.5rem;
    }

    select {
      font-size: 0.75rem;
    }

    .filters {
      flex-wrap: wrap;
      width: 100vw;
    }

    .filter-label {
      flex-basis: 100%;
      text-align: center;
    }

    .reported-issues-label {
      font-size: 1.2rem;
    }

    .button {
      font-size: 1rem;
    }

    .logo {
      margin-top: 2rem;
      height: 3rem;
    }

    .we-move {
      margin-top: 3rem;
      font-size: 2.65rem;
    }

    .slogan-title {
      margin: 5rem 0.5rem 2.5rem 0.5rem;
      font-size: 1.65rem;
      font-weight: 600;
      letter-spacing: 0.01rem;
      text-align: center;
    }

    .slogan-text {
      font-size: 1.3rem;
    }

    .action-buttons {
      display: block;
      text-align: center;
      margin-top: 4rem;
    }

    #button-report-issue {
      margin-top: 2.5rem;
      width: 18rem;
    }

    #button-find-issues {
      width: 18rem;
    }

    .background {
      height: 1200px;
    }

    .background::before {
      background-position: 45%;
    }

    textarea {
      margin-left: 0;
      margin-bottom: 0.1rem;
      width: 92.5vw;
      font-size: 0.85rem;
    }

    textarea::placeholder {
      font-size: 0.85rem;
    }
  }
</style>
