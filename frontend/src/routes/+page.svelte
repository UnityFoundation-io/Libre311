<script>
  import { Loader } from "@googlemaps/js-api-loader";
  import { fade, scale, blur } from "svelte/transition";
  import { quintOut } from "svelte/easing";
  import { onMount } from "svelte";
  import { inview } from "svelte-inview";
  import { browser } from "$app/environment";
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
  import issuePinSVG from "../icons/issuepin.svg";
  import issueAddress from "../stores/issueAddress";
  import issueTime from "../stores/issueTime";
  import issueType from "../stores/issueType";
  import issueDetail from "../stores/issueDetail";
  import issueDescription from "../stores/issueDescription";
  import issueSubmitterName from "../stores/issueSubmitterName";
  import issueSubmitterContact from "../stores/issueSubmitterContact";
  import userCurrentLocation from "../stores/userCurrentLocation";
  import resetDate from "../stores/resetDate";
  import issueDetailList from "../stores/issueDetailList";
  import footerDivHeight from "../stores/footerDivHeight";
  import {
    totalSize,
    totalPages,
    currentPage,
    itemsPerPage,
  } from "../stores/pagination";
  import footerSelector from "../stores/footerSelector";
  import DateRangePicker from "$lib/DateRangePicker.svelte";
  import Modal from "$lib/Modal.svelte";
  import Footer from "$lib/Footer.svelte";
  import messages from "$lib/messages.json";
  import colors from "$lib/colors.json";
  import "$lib/global.css";

  // Configure the backend path
  axios.defaults.baseURL = import.meta.env.VITE_BACKEND_URL;
  const apiKey = import.meta.env.VITE_GOOGLE_API_KEY;

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
  const fileNameMaxLength = 35;
  const waitTime = 1000;

  // Page Height
  let pageHeightIssues = 1650;
  let pageHeightIssues677Portrait = 1150;
  let pageHeightIssues844Portrait = 1350;
  let pageHeightIssues926Portrait = 1550;

  let pageWidthIssues677Landscape = 1050;
  let pageWidthIssues844Landscape = 1100;
  let pageWidthIssues926Landscape = 1100;

  itemsPerPage.set(10);

  let issueTypeTrimCharacters = 20;

  let serviceCodes = [];

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
    showModal = false,
    showFooter = true,
    showTable = true,
    heatmapVisible = true,
    landscapeMode = false;

  let map,
    heatmap,
    geocoder,
    bounds,
    selectedIssue,
    heatmapControlIndex,
    timer,
    backgroundSelector,
    inputIssueAddressSelector,
    issueDetailSelector,
    issueTypeSelectSelector,
    issueDetailSelectSelector,
    findIssuesButtonSelector,
    reportIssuesButtonSelector;

  let zoom = 15;
  let markers = [];
  let heatmapData = [];
  let currentPositionMarker;

  let issuesData = [];

  let filteredIssuesData;

  let filterIssueType = { service_code: "", service_name: "" };
  let filterStartDate = "",
    filterEndDate = "";

  // Image Safe Search
  let selectedFile = null,
    messageSuccess = "",
    messageRejectedOne = "",
    messageRejectedTwo = "";

  // Detect Landscape Mode
  $: if (browser && (window.orientation === 90 || window.orientation === -90)) {
    landscapeMode = true;
    issueTypeTrimCharacters = 30;
  }

  // Filtering Results
  $: if (filterIssueType.service_code !== "") {
    const serviceName = serviceCodes.find(
      (service) => filterIssueType.service_code === service.service_code
    );
    filterIssueType.service_name = serviceName.service_name;
  }

  $: if (filterStartDate !== "" && filterEndDate !== "") {
    clearData();
    setTimeout(() => {
      getIssues(0, true);
    }, 100);
  }

  const loader = new Loader({
    apiKey: "AIzaSyC_RuNsPOuWzMq7oiWNDxJoiqGZrOky9Kk",
    version: "weekly",
    libraries: ["places", "visualization"],
  });

  $: if (reportNewIssueStep6) {
    setTimeout(() => {
      const divSuccessMessage = document.getElementById("stepSubmitted");
      divSuccessMessage.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });

      setTimeout(() => {
        resetState();
        reportNewIssueStep6 = false;
        backgroundSelector.style.height = $footerDivHeight + "px";
      }, 3000);

      setTimeout(() => (showFooter = true), 4000);
    }, 100);
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

  const clearUploadMessages = () => {
    messageSuccess = "";
    messageRejectedOne = "";
    messageRejectedTwo = "";
  };

  const handleFileChange = (event) => {
    clearUploadMessages();
    selectedFile = event.target.files[0];
  };

  const handleSubmit = async () => {
    // Convert the selected image file to a data URL
    const imageUrl = await new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (event) => resolve(event.target.result);
      reader.readAsDataURL(selectedFile);
    });

    // Call the detectExplicitContent function with the image data URL
    const isExplicit = await detectExplicitContent(imageUrl, apiKey);

    if (isExplicit) {
      messageRejectedOne =
        messages["report.issue"]["uploaded.message.rejected-one"];
      messageRejectedTwo =
        messages["report.issue"]["uploaded.message.rejected-two"];

      selectedFile = null;
    } else {
      messageSuccess = messages["report.issue"]["uploaded.message.success"];
      selectedFile = null;

      // Save the image to the Server Locally
    }
  };

  const detectExplicitContent = async (imageUrl, apiKey) => {
    const apiEndpoint = "https://vision.googleapis.com/v1/images:annotate";
    const base64Content = imageUrl.split(",")[1];

    const requestBody = {
      requests: [
        {
          image: {
            content: base64Content,
          },
          features: [
            {
              type: "SAFE_SEARCH_DETECTION",
            },
          ],
        },
      ],
    };

    try {
      const response = await fetch(`${apiEndpoint}?key=${apiKey}`, {
        method: "POST",
        body: JSON.stringify(requestBody),
        headers: {
          "Content-Type": "application/json",
        },
      });

      if (!response.ok) {
        throw new Error(`HTTP error: ${response.status}`);
      }

      const result = await response.json();
      const safeSearch = result.responses[0].safeSearchAnnotation;

      const explicitLevels = ["LIKELY", "VERY_LIKELY"];
      const isExplicit =
        explicitLevels.includes(safeSearch.adult) ||
        explicitLevels.includes(safeSearch.violence) ||
        explicitLevels.includes(safeSearch.racy);

      return isExplicit;
    } catch (error) {
      console.error("Error:", error);
    }
  };

  const loadMoreResults = async (e) => {
    if (e.detail.inView && hasMoreResults) {
      if (Number($currentPage) + 1 < $totalPages) {
        $currentPage++;
        await getIssues($currentPage);
        clearHeatmap();
        await addIssuesToMap();
      }
    }
  };

  const options = {
    rootMargin: "20px",
    unobserveOnEnter: true,
  };

  const getAllServiceCodes = async () => {
    const res = await axios.get(`/services`);
    if (res.data) serviceCodes = res.data;
  };

  const getServiceDefinition = async (serviceId) => {
    const res = await axios.get(`/services/${serviceId}`);
    issueDetailList.set(res.data.attributes[0]);
  };

  const populateIssueTypeSelectDropdown = () => {
    const defaultOption = document.createElement("option");
    defaultOption.text = "Issue Type";
    defaultOption.value = "";
    defaultOption.disabled = true;
    defaultOption.selected = true;

    issueTypeSelectSelector.add(defaultOption);

    for (let i = 0; i < serviceCodes.length; i++) {
      const option = document.createElement("option");
      option.text = serviceCodes[i].service_name;
      option.value = serviceCodes[i].service_code;
      issueTypeSelectSelector.add(option);
    }
  };

  const populateIssueDetailList = () => {
    const defaultOption = document.createElement("option");
    defaultOption.text = "Issue Detail";
    defaultOption.value = "";
    defaultOption.disabled = true;
    defaultOption.selected = true;

    issueDetailSelectSelector.add(defaultOption);

    for (let i = 0; i < $issueDetailList.values.length; i++) {
      const option = document.createElement("option");
      option.text = $issueDetailList.values[i].name;
      option.value = $issueDetailList.values[i].key;
      issueDetailSelectSelector.add(option);
    }
  };

  const getIssues = async (page = 0, displayIssuesInMap = false) => {
    let res;

    res = await axios.get(
      `/requests?page_size=${$itemsPerPage}&page=${page}&service_code=${filterIssueType.service_code}&start_date=${filterStartDate}&end_date=${filterEndDate}`
    );

    if (
      res.data?.length > 0 &&
      JSON.stringify(issuesData) !== JSON.stringify(res.data)
    ) {
      if (issuesData) issuesData = [...issuesData, ...res.data];
      else issuesData = [...res.data];

      filteredIssuesData = issuesData;
    }
    totalSize.set(res.headers["page-totalsize"]);
    totalPages.set(res.headers["page-totalpages"]);
    currentPage.set(res.headers["page-pagenumber"]);

    if (res.data && res.data?.length < Number($itemsPerPage)) {
      hasMoreResults = false;
    }

    if (res.data?.length > 0) {
      if (displayIssuesInMap) await addIssuesToMap();
    } else {
      setNewCenter("38.95180510457306", "-92.32740864543621", 12); // Columbia, Missouri
    }
  };

  const postIssue = async () => {
    let attributes = `?service_code=${$issueType.id}&address_string=${$issueAddress}&lat=${$userCurrentLocation.lat}&long=${$userCurrentLocation.lng}`;

    $issueDetail.forEach((attr) => {
      attributes += "&attribute[" + $issueDetailList.code + "]=" + attr.id;
    });

    if ($issueDescription) attributes += "&description=" + $issueDescription;
    if ($issueSubmitterName) {
      const nameParts = $issueSubmitterName.trim().split(" ");
      const first_name = nameParts[0];
      const last_name = nameParts.slice(1).join(" ");

      if (last_name)
        attributes += "&first_name=" + first_name + "&last_name=" + last_name;
      else attributes += "&first_name=" + first_name;
    }

    if ($issueSubmitterContact)
      attributes += "&email=" + $issueSubmitterContact;

    const data = new URLSearchParams(attributes);

    // change once we can upload images
    // const mediaUrl = '';
    // if(mediaUrl) attributes += "&media_url=" + mediaUrl;

    axios.post("/requests.json", data, {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  };

  const clearData = () => {
    issuesData = "";
    filteredIssuesData = "";
    totalPages.set(0);
    currentPage.set(0);
    hasMoreResults = true;
    clearMarkers();
    clearHeatmap();
  };

  const clearHeatmap = () => {
    heatmap.setMap(null);
    heatmapData = [];
    heatmap.setData(heatmapData);
  };

  const clearFilters = () => {
    filterStartDate = "";
    filterEndDate = "";
    filterIssueType = { service_code: "", service_name: "" };
  };

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const toggleTable = () => {
    showTable = !showTable;
  };

  const geocodeLatLng = (lat, lng) => {
    const latlng = { lat: parseFloat(lat), lng: parseFloat(lng) };
    geocoder.geocode({ location: latlng }, (results, status) => {
      if (status === "OK") {
        if (results[0]) {
          issueAddress.set(results[0].formatted_address);
          inputIssueAddressSelector.value = results[0].formatted_address;
        } else {
          console.log(messages["geocode"]["empty.results"]);
        }
      } else {
        console.log(`${messages["geocode"]["error"]} ${status}`);
      }
    });
  };

  const setNewCenter = (lat, lng, zoom = 15) => {
    let newCenter = new google.maps.LatLng(lat, lng);
    map.setCenter(newCenter);
    setNewZoom(zoom);
  };

  const setNewZoom = (zoomLevel) => {
    map.setZoom(zoomLevel);
  };

  const successCallback = (position) => {
    $userCurrentLocation = {
      lat: position.coords.latitude ?? "38.95180510457306",
      lng: position.coords.longitude ?? "-92.32740864543621",
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
    $issueDetail = [];
    $issueType = null;
    $issueDetailList = null;
    inputIssueAddressSelector.value = "";
    selectedFile = null;
    clearUploadMessages();
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

  const formatSimpleDate = (dateString) => {
    const [datePart, timePart] = dateString.split("T");
    const [year, month, day] = datePart.split("-");
    return `${month}-${day}-${year}`;
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

    const formattedDate = `${
      months[monthIndex]
    } ${day}, ${year} ${hours}:${minutes.toString().padStart(2, "0")}`;
    return formattedDate;
  };

  const addIssuesToMap = async () => {
    clearMarkers();

    if (filteredIssuesData && filteredIssuesData.length > 0) {
      filteredIssuesData.forEach((issue) => {
        let marker;

        marker = new google.maps.Marker({
          position: {
            lat: parseFloat(issue.lat),
            lng: parseFloat(issue.long),
          },
          map: map,
          title: issue.name,
          icon: {
            scaledSize: new google.maps.Size(25, 25),
            url: issuePinSVG,
            anchor: new google.maps.Point(12, 12),
          },
        });

        markers.push(marker);

        heatmapData.push(
          new google.maps.LatLng(parseFloat(issue.lat), parseFloat(issue.long))
        );
      });

      heatmap = new google.maps.visualization.HeatmapLayer({
        data: heatmapData,
      });

      if (
        map.controls[window.google.maps.ControlPosition.BOTTOM_LEFT].length ===
        2
      ) {
        const heatmapControl = createCustomControl("Heatmap", function () {
          heatmapVisible = !heatmapVisible;
          if (heatmapVisible) {
            for (var i = 0; i < markers.length; i++) {
              markers[i].setMap(null);
            }
            const button = document.getElementById("Heatmap-control");
            button.innerHTML = "Markers";

            heatmap.setMap(map);
          } else {
            const button = document.getElementById("Heatmap-control");
            button.innerHTML = "Heatmap";

            heatmap.setMap(null);
            for (var i = 0; i < markers.length; i++) {
              markers[i].setMap(map);
            }
          }
        });

        heatmapControlIndex =
          map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(
            heatmapControl
          ) - 1;
      }

      if (heatmapVisible) heatmap.setMap(map);

      heatmap.set("radius", 15);
      heatmap.set("opacity", 0.8);

      if (heatmapVisible) toggleMarkers();

      const gradient = [
        "rgba(0, 255, 255, 0)",
        "rgba(0, 255, 255, 1)",
        "rgba(0, 191, 255, 1)",
        "rgba(0, 127, 255, 1)",
        "rgba(0, 63, 255, 1)",
        "rgba(0, 0, 255, 1)",
        "rgba(0, 0, 223, 1)",
        "rgba(0, 0, 191, 1)",
        "rgba(0, 0, 159, 1)",
        "rgba(0, 0, 127, 1)",
        "rgba(63, 0, 91, 1)",
        "rgba(127, 0, 63, 1)",
        "rgba(191, 0, 31, 1)",
        "rgba(255, 0, 0, 1)",
      ];
      heatmap.set("gradient", gradient);

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

  const toggleMarkers = () => {
    for (var i = 0; i < markers.length; i++) {
      if (markers[i].getMap() === null) {
        markers[i].setMap(map);
      } else {
        markers[i].setMap(null);
      }
    }
  };

  const createCustomControl = (text, clickHandler) => {
    const controlButton = document.createElement("button");
    controlButton.id = text + "-control";
    controlButton.style.backgroundColor = "#fff";
    controlButton.style.border = "2px solid #fff";
    controlButton.style.borderRadius = "3px";
    controlButton.style.boxShadow = "0 2px 6px rgba(0, 0, 0, 0.3)";
    controlButton.style.cursor = "pointer";
    controlButton.style.userSelect = "none";
    if (window.innerWidth < 677) controlButton.style.marginBottom = "1rem";
    else controlButton.style.marginBottom = "0.3rem";
    controlButton.style.marginLeft = "0.3rem";
    controlButton.style.width = "fit-content";
    controlButton.style.height = "20px";
    controlButton.innerText = text;

    controlButton.addEventListener("click", clickHandler);

    return controlButton;
  };

  const adjustFooter = () => {
    if ($footerSelector) {
      footerDivHeight.set(
        $footerSelector.offsetTop + $footerSelector.offsetHeight
      );

      backgroundSelector.style.height = $footerDivHeight + "px";
    }
  };

  ////////////// Screen Adjustments //////////////////////////////////////////////////////////////////////

  ////////////// Portrait //////////////////

  $: if (browser && window.innerHeight <= 677 && window.innerWidth <= 375) {
    pageHeightIssues = pageHeightIssues677Portrait;
    issueTypeTrimCharacters = 15;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone SE Portrait");
  }

  $: if (
    browser &&
    window.innerHeight <= 844 &&
    window.innerHeight > 677 &&
    window.innerWidth <= 390
  ) {
    pageHeightIssues = pageHeightIssues844Portrait;
    issueTypeTrimCharacters = 15;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone 12 Pro Portrait");
  }

  $: if (
    browser &&
    window.innerHeight <= 926 &&
    window.innerHeight > 844 &&
    window.innerWidth <= 428
  ) {
    pageHeightIssues = pageHeightIssues926Portrait;
    issueTypeTrimCharacters = 15;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone 13 Pro Max Portrait");
  }

  $: if (browser && window.innerWidth > 926) {
    pageHeightIssues = pageHeightIssues;
    issueTypeTrimCharacters = 20;
    setTimeout(() => adjustFooter(), 300);
    console.log("Desktop");
  }

  ////////////// Landscape //////////////////

  $: if (browser && window.innerWidth <= 677 && window.innerHeight <= 375) {
    pageHeightIssues = pageWidthIssues677Landscape;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone SE Landscape");
  }

  $: if (
    browser &&
    window.innerWidth <= 844 &&
    window.innerWidth > 677 &&
    window.innerHeight <= 390
  ) {
    pageHeightIssues = pageWidthIssues844Landscape;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone 12 Pro Landscape");
  }

  $: if (
    browser &&
    window.innerWidth <= 926 &&
    window.innerWidth > 844 &&
    window.innerHeight <= 428
  ) {
    pageHeightIssues = pageWidthIssues926Landscape;
    setTimeout(() => adjustFooter(), 300);
    console.log("iPhone 13 Pro Max Landscape");
  }

  ////////////// Screen Adjustments //////////////////////////////////////////////////////////////////////

  onMount(async () => {
    // Warn user before leaving the website
    window.addEventListener("beforeunload", handleBeforeUnload);

    setTimeout(() => adjustFooter(), 300);

    loadColorPalette();

    scrollToTop();

    await getAllServiceCodes();

    await getIssues();

    // Trigger the Svelte Transitions
    fadeInBackground = true;
    openLogo = true;
    openWeMove = true;

    loader.load().then(async (google) => {
      map = new google.maps.Map(document.getElementById("map"), {
        zoom: zoom,
        center: { lat: 38.6740015313782, lng: -90.453269188364 },
        mapTypeControl: false,
      });

      geocoder = new google.maps.Geocoder();

      bounds = new google.maps.LatLngBounds();

      inputIssueAddressSelector = document.getElementById("pac-input");

      const searchBox = new google.maps.places.SearchBox(
        inputIssueAddressSelector
      );

      const mapControl = createCustomControl("Map", function () {
        map.setMapTypeId(google.maps.MapTypeId.ROADMAP);
      });

      const satelliteControl = createCustomControl("Satellite", function () {
        map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
      });

      map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(mapControl);
      map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(
        satelliteControl
      );
      map.controls[google.maps.ControlPosition.TOP_LEFT].push(
        inputIssueAddressSelector
      );

      const icon = {
        url: currentLocationSVG,
        size: new google.maps.Size(71, 71),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(17, 34),
        scaledSize: new google.maps.Size(55, 55),
      };

      currentPositionMarker = new google.maps.Marker({
        map,
        icon,
        position: map.getCenter(),
        title: messages["map"]["marker.title"],
      });

      map.addListener("center_changed", async () => {
        if (!findReportedIssue) {
          const center = map.getCenter();
          currentPositionMarker.setPosition(center);

          clearTimeout(timer);
          timer = setTimeout(() => {
            geocodeLatLng(center.lat(), center.lng());
            console.log("geocoding...");
          }, waitTime);
        }
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
    height="{pageHeightIssues}px"
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
    <div style="display: flex; align-items: center; z-index: 1" id="logo-div">
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
        {messages["home"]["tagline.two"]}
      </div>

      <div class="action-buttons">
        {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5}
          <button
            bind:this="{findIssuesButtonSelector}"
            class="button"
            class:button-find-issue-disabled="{showModal}"
            disabled="{showModal}"
            id="button-find-issues"
            on:click="{async () => {
              if (reportNewIssueStep6) return;

              // Clears the current position marker from the map
              currentPositionMarker.setMap(null);
              inputIssueAddressSelector = document.getElementById('pac-input');
              inputIssueAddressSelector.value = '';

              backgroundSelector.style.height = pageHeightIssues + 'px';

              if (!findReportedIssue) {
                setTimeout(() => {
                  findIssuesButtonSelector.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start',
                  });
                }, 10);
                showFooter = false;
                findReportedIssue = true;
                if (!filteredIssuesData) await getIssues();

                addIssuesToMap();
              } else {
                scrollToTop();

                findReportedIssue = false;
                showFilters = false;
                showTable = true;

                backgroundSelector.style.height = $footerDivHeight + 'px';
                setTimeout(() => (showFooter = true), 400);
                clearData();
                clearFilters();
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
                alt="close find an issue"
                style="vertical-align: -0.3rem; margin-right: 1.3rem; margin-left: -0.7rem"
                height="25rem"
              />
            {/if}
            {messages["home"]["button.find.issue.label"]}
          </button>
        {/if}

        {#if !findReportedIssue}
          <button
            bind:this="{reportIssuesButtonSelector}"
            class="button"
            class:collapse-margin="{reportNewIssue ||
              reportNewIssueStep2 ||
              reportNewIssueStep3 ||
              reportNewIssueStep4 ||
              reportNewIssueStep5 ||
              reportNewIssueStep6}"
            id="button-report-issue"
            on:click="{() => {
              if (reportNewIssueStep6) return;

              const ReportIssueButton = document.getElementById(
                'button-report-issue'
              );

              if (
                map.controls[window.google.maps.ControlPosition.BOTTOM_LEFT]
                  .length === 3
              )
                map.controls[
                  window.google.maps.ControlPosition.BOTTOM_LEFT
                ].removeAt(heatmapControlIndex);

              currentPositionMarker.setMap(map);

              backgroundSelector.style.height = pageHeightIssues + 'px';

              setTimeout(() => {
                reportIssuesButtonSelector.scrollIntoView({
                  behavior: 'smooth',
                  block: 'start',
                });
              }, 10);
              if (!reportNewIssue && !currentStep) {
                showFooter = false;
                reportNewIssue = true;
                reduceBackGroundOpacity = false;
                currentStep = 1;
              } else if (reportNewIssue) {
                reportNewIssue = false;
                resetState();

                backgroundSelector.style.height = $footerDivHeight + 'px';
                setTimeout(() => (showFooter = true), 400);
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
    {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5 && !reportNewIssueStep6 && !findReportedIssue && showFooter}
      <Footer bind:this="{$footerSelector}" backgroundColor="{secondaryOne}" />
    {/if}
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
              bind:this="{issueTypeSelectSelector}"
              on:change="{async (e) => {
                issueType.set({
                  id: e.target.value,
                  name: e.target.options[e.target.selectedIndex].text,
                });

                await getServiceDefinition(e.target.value);

                // Remove any options
                while (issueDetailSelectSelector.firstChild) {
                  issueDetailSelectSelector.removeChild(
                    issueDetailSelectSelector.firstChild
                  );
                }

                populateIssueDetailList();
              }}"></select>

            {#if $issueDetailList?.description}
              <div class="step-two-feature-type-label">
                {$issueDetailList.description}
              </div>
            {/if}

            {#if $issueDetailList}
              <select
                class="step-two-select-detail"
                style="display: block"
                bind:this="{issueDetailSelectSelector}"
                on:load="{populateIssueDetailList}"
                on:change="{(e) => {
                  if ($issueDetail) {
                    if (
                      !$issueDetail.find(
                        (selection) => selection.id === e.target.value
                      )
                    )
                      issueDetail.update((currentSelections) => [
                        ...currentSelections,
                        {
                          name: e.target.options[e.target.selectedIndex].text,
                          id: e.target.value,
                        },
                      ]);
                  } else
                    issueDetail.set({
                      name: e.target.options[e.target.selectedIndex].text,
                      id: e.target.value,
                    });
                  e.target.options[0].selected = true;
                }}"></select>
            {/if}

            {#if $issueDetail?.length > 0}
              {#each $issueDetail as selection}
                <div class="issue-detail-selected">
                  {selection.name}
                  <!-- svelte-ignore a11y-click-events-have-key-events -->
                  <img
                    src="{closeSVG}"
                    class="white-closeSVG"
                    alt="remove issue detail"
                    width="14rem"
                    on:click="{async () => {
                      $issueDetail = $issueDetail.filter(
                        (issueDTL) => issueDTL.id !== selection.id
                      );
                    }}"
                  />
                </div>
              {/each}
            {/if}

            {#if $issueType !== null && $issueDetail.find((selection) => selection.name === "Other")}
              <div style="display: inline-block">
                <div class="step-two-word-count">* Required field</div>

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

          <div style="margin-top: 1rem">
            <form on:submit|preventDefault="{handleSubmit}">
              <div style="display: flex">
                <label
                  for="image-uploads"
                  class="upload-image"
                  id="upload-image-label"
                >
                  {#if selectedFile}
                    {#if selectedFile.name.length > fileNameMaxLength}
                      {selectedFile.name.slice(0, fileNameMaxLength)}...
                    {:else}
                      {selectedFile.name}
                    {/if}
                  {:else}
                    {messages["report.issue"]["label.choose.image"]}
                  {/if}

                  <!-- svelte-ignore a11y-img-redundant-alt -->
                  <img
                    src="{cameraSVG}"
                    alt="take photo"
                    width="37rem"
                    style="padding: 0 0.3rem 0 0.3rem; vertical-align: middle; background-color: white; border-radius: 10px; margin-left: 0.5rem; margin-right: 0.5rem; cursor: pointer"
                  />
                </label>
                <input
                  type="file"
                  id="image-uploads"
                  accept="image/*"
                  capture="camera"
                  style="display: none"
                  on:change="{handleFileChange}"
                />

                <button
                  type="submit"
                  class="button"
                  class:upload="{selectedFile}"
                  class:disabled-button-upload="{!selectedFile}"
                  disabled="{!selectedFile}">Upload</button
                >
              </div>
            </form>

            {#if messageRejectedOne}
              <div class="upload-message">{messageRejectedOne}</div>
              <div class="upload-message">{messageRejectedTwo}</div>
            {:else if messageSuccess}
              <!-- svelte-ignore a11y-img-redundant-alt -->
              <div class="upload-message">
                <img
                  src="{imageSVG}"
                  alt="issue image"
                  height="25rem"
                  style="vertical-align: -0.3rem; margin-right: 0.5rem"
                  class="white-icon"
                />{messageSuccess}
              </div>
            {/if}

            <button
              class="button back-button"
              style="margin-bottom: 1.25rem; margin-top: 2rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                reportNewIssueStep2 = true;
                setTimeout(() => {
                  if ($issueType !== null)
                    issueTypeSelectSelector.value = $issueType.id;
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
                $issueDetail === null ||
                selectedFile}"
              disabled="{$issueType === null ||
                $issueDetail === null ||
                selectedFile}"
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
              {$issueType.name}
            </div>
          </div>
          <div class="step-five-issue-detail-label">
            {messages["report.issue"]["label.review.issue.detail"]}
            <div class="step-five-issue-detail">
              {#each $issueDetail as detail, i}
                <span style="margin-right: 1rem">{i + 1}-{detail.name}</span>
              {/each}
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
              postIssue();
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
          id="stepSubmitted"
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
                  setTimeout(() => populateIssueTypeSelectDropdown(), 10);

                  // We add delay to support the fade in
                  setTimeout(() => {
                    if ($issueType !== null) {
                      issueTypeSelectSelector.value = $issueType.id;
                      populateIssueDetailList();
                    }
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
        <div id="map"></div>

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
              <span
                on:click="{() => (showFilters = !showFilters)}"
                style="cursor: pointer"
              >
                -
              </span>
            {:else}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <span
                style="cursor: pointer"
                on:click="{() => {
                  showFilters = !showFilters;
                  // Wait for the DOM to render the Dropdown
                  setTimeout(() => populateIssueTypeSelectDropdown(), 10);
                }}"
              >
                +
              </span>
            {/if}
          </div>

          {#if showFilters}
            <div class="filters-wrapper">
              <div class="filters">
                <select
                  class="select-filter"
                  bind:this="{issueTypeSelectSelector}"
                  on:change="{async (e) => {
                    clearData();
                    filterIssueType = { service_code: e.target.value };
                    await getIssues();
                    setTimeout(async () => await addIssuesToMap(), 1000);
                  }}"></select>

                <select
                  class="select-filter"
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
                    if (e.detail.length === 2) {
                      filterStartDate = e.detail[0];
                      filterEndDate = e.detail[1];
                    }
                    findIssuesButtonSelector.scrollIntoView({
                      behavior: 'smooth',
                      block: 'start',
                    });
                  }}"
                />
              </div>

              <div class="filter-selection">
                {#if filterIssueType.service_name !== ""}
                  <div class="filter-selection-label">
                    {filterIssueType.service_name}
                    <!-- svelte-ignore a11y-click-events-have-key-events -->
                    <img
                      src="{closeSVG}"
                      class="white-closeSVG"
                      alt="remove filter"
                      width="14rem"
                      on:click="{async () => {
                        filterIssueType = {
                          service_code: '',
                          service_name: '',
                        };
                        issuesData = '';
                        hasMoreResults = true;
                        clearHeatmap();

                        await getIssues();
                        issueTypeSelectSelector.selectedIndex = 0;
                        addIssuesToMap();
                      }}"
                    />
                  </div>
                {/if}

                {#if filterStartDate !== "" && filterEndDate !== ""}
                  <div class="filter-selection-label">
                    From {formatSimpleDate(filterStartDate)} to {formatSimpleDate(
                      filterEndDate
                    )}

                    <!-- svelte-ignore a11y-click-events-have-key-events -->
                    <img
                      src="{closeSVG}"
                      class="white-closeSVG"
                      alt="remove filter"
                      width="14rem"
                      on:click="{async () => {
                        filterStartDate = '';
                        filterEndDate = '';

                        clearData();

                        resetDate.set(true);
                        await getIssues();

                        addIssuesToMap();
                      }}"
                    />
                  </div>
                {/if}
              </div>
            </div>
          {/if}

          <div class="reported-issues-label">
            {messages["find.issue"]["label.reported.issues"]}
            {#if showTable}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <span on:click="{toggleTable}" style="cursor: pointer">-</span>
            {:else}
              <!-- svelte-ignore a11y-click-events-have-key-events -->
              <span on:click="{toggleTable}" style="cursor: pointer">+</span>
            {/if}
          </div>

          {#if showTable}
            <table
              class="issues-table"
              class:table-expanded="{!showFilters}"
              class:table-contracted="{showFilters}"
            >
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
                      class="td-issue-type"
                      on:click="{() => {
                        toggleDetails(issue.service_request_id);
                        selectedIssue = issue;
                      }}"
                    >
                      {#if issue.service_name.length > issueTypeTrimCharacters}
                        {issue.service_name.slice(
                          0,
                          issueTypeTrimCharacters
                        )}...
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
                      }}">{issue.description ?? "-"}</td
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
                    <tr
                      style="background-color: {hexToRGBA(secondaryTwo, 0.1)}"
                    >
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
                            geocodeLatLng(
                              selectedIssue.lat,
                              selectedIssue.long
                            );
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
        {/if}
      </div>
      <div id="position-element" style="display: none"></div>
    </div>
  </div>
{/if}
