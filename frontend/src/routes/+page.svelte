<script>
  import { fade, scale, blur } from "svelte/transition";
  import { quintOut } from "svelte/easing";
  import { onMount } from "svelte";
  import { inview } from "svelte-inview";
  import { browser } from "$app/environment";
  import FontFaceObserver from "fontfaceobserver";
  import axios from "axios";
  import MultiSelect from "svelte-multiselect";
  import logo from "$lib/logo.webp";
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
  import issuePinSelectedSVG from "../icons/issuepinselected.svg";
  import forbiddenSVG from "../icons/forbidden.svg";
  import mylocationSVG from "../icons/mylocation.svg";
  import fireSVG from "../icons/fire.svg";
  import issueAddress from "../stores/issueAddress";
  import seeMoreHeight from "../stores/seeMoreHeight";
  import issueAddressCoordinates from "../stores/issueAddressCoordinates";
  import issueTime from "../stores/issueTime";
  import issueType from "../stores/issueType";
  import issueDetail from "../stores/issueDetail";
  import issueDescription from "../stores/issueDescription";
  import issueSubmitterName from "../stores/issueSubmitterName";
  import issueSubmitterContact from "../stores/issueSubmitterContact";
  import userCurrentLocation from "../stores/userCurrentLocation";
  import resetDate from "../stores/resetDate";
  import issueDetailList from "../stores/issueDetailList";
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
  import Recaptcha from "$lib/Recaptcha.svelte";
  import messages from "$lib/messages.json";
  import colors from "$lib/colors.json";
  import "$lib/global.css";
  import "$lib/spinner.css";

  import mapProvider from "$lib/mapProvider.json";

  const provider = mapProvider["provider"]

  // Configure the backend path
  axios.defaults.baseURL = import.meta.env.VITE_BACKEND_URL;
  const apiKey = import.meta.env.VITE_GOOGLE_API_KEY;
  const sitekey = import.meta.env.VITE_GOOGLE_RECAPTCHA_KEY;

  const hexToRGBA = (hex, alpha = 1) => {
    const r = parseInt(hex.slice(1, 3), 16);
    const g = parseInt(hex.slice(3, 5), 16);
    const b = parseInt(hex.slice(5, 7), 16);

    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  // Constants
  const maxCharactersLength = 4000;
  const minSubmitterNameLength = 3;
  const minOtherDescriptionLength = 10;
  const startRendering = 2000;
  const primaryOne = colors["primary.one"];
  const primaryOneAlpha = hexToRGBA(primaryOne, 0.6);
  const primaryTwo = colors["primary.two"];
  const secondaryOne = colors["secondary.one"];
  const secondaryTwo = colors["secondary.two"];
  const accentOne = colors["accent.one"];
  const accentTwo = colors["accent.two"];
  const issueDescriptionTrimCharacters = 36;
  const waitTime = 1000;
  const minAddressCharacters = 15;
  const debounceTime = 1000;

  // Page Height
  let pageHeight = 1650;

  itemsPerPage.set(10);

  let issueTypeTrimCharacters = 20;

  let serviceCodes = [];

  let openLogo = false,
    fadeInBackground = false,
    reduceBackGroundOpacity = false,
    reportNewIssue = false,
    reportNewIssueStep2 = false,
    reportNewIssueStep3 = false,
    reportNewIssueStep4 = false,
    reportNewIssueStep5 = false,
    reportNewIssueStep6 = false,
    currentStep = null,
    findReportedIssue = false,
    postingOfflineIssue = false,
    notifyOfflineIssuePosted = false,
    geocodeError = false,
    showFilters = false,
    hasMoreResults = true,
    showModal = false,
    showFooter = true,
    showTable = true,
    seeMore = false,
    spinner = false,
    heatmapVisible = false,
    postingError = false,
    issuesRefs = {},
    multiSelectOptions = [],
    invalidOtherDescription = {
      message: messages["report.issue"]["textarea.description.error"],
      visible: false,
    },
    invalidSubmitterName = {
      message: messages["report.issue"]["input.submitter.name.error"],
      visible: false,
    },
    invalidEmail = {
      message: messages["report.issue"]["input.email.error"],
      visible: false,
    },
    fileName = "report.csv",
    invalidOfflineAddress = false,
    isAuthenticated = false;

  let offlineAddressRegex = /^[0-9]+[a-zA-Z0-9&\-',. ]+$/gm;

  let submitterNameRegex = /^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð\-'. ]+$/gm;

  let emailRegex =
    /^([a-zA-Z0-9!#$%&'*+\/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+\/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)$/gm;

  let map,
    heatmap,
    geocoder,
    bounds,
    selectedIssue,
    selectedIssueMarker,
    heatmapControlIndex,
    timer,
    recaptcha,
    token,
    backgroundSelector,
    inputIssueAddressSelector,
    issueDetailSelector,
    offlineAddressInputSelector,
    tableSelector,
    issueTypeSelectSelector,
    issueDetailSelectSelector,
    findIssuesButtonSelector,
    reportIssuesButtonSelector,
    selected,
    isOnline,
    wasOnline,
    imageData,
    timeoutId;

  let markerGroup;

  $: if (issueTypeSelectSelector !== undefined)
    setTimeout(() => {
      if (issueTypeSelectSelector?.options?.length === 0)
        populateIssueTypeSelectDropdown();
    }, 200);

  let zoom = 15;
  let markers = [];
  let heatmapData = [];
  let currentPositionMarker;

  let issuesData = [];

  let filteredIssuesData = [];

  let filterIssueType = { service_code: "", service_name: "" };
  let filterStartDate = "",
    filterEndDate = "";

  // Image Safe Search
  let selectedFile = null,
    selectedFileSrc = null,
    messageSuccess = "",
    messageRejectedOne = "",
    messageRejectedTwo = "",
    mediaUrl;

  // Locks the background scroll when modal is open
  $: if (browser && showModal) {
    document.body.classList.add("modal-open");
  } else if (browser && !showModal) {
    document.body.classList.remove("modal-open");
  }



  const getOrientation = () => {
    let previousState;
    scrollToTop();

    if (reportNewIssue) previousState = "reportNewIssue";
    if (findReportedIssue) previousState = "findReportedIssue";

    if (reportNewIssue) reportNewIssue = false;
    if (findReportedIssue) findReportedIssue = false;

    setTimeout(() => {
      showFooter = true;
      setTimeout(async () => {
        await adjustFooter();
      }, 100);
    }, 400);

    if (previousState === "reportNewIssue") {
      setTimeout(() => {
        reportNewIssue = true;
        adjustMap();
      }, 400);
    }

    if (previousState === "findReportedIssue") {
      setTimeout(() => {
        findReportedIssue = true;
        setTimeout(async () => await adjustTable(), 100);
      }, 400);
    }
  };

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
        postingError = false;
      }, 3000);
    }, 100);
  }

  let visibleDetails = new Set();

  const getTokenInfo = async () => {
    let res;

    try {
      res = await axios.get("/token_info", { withCredentials: true });
    } catch (err) {
      console.info(err);
    }
    if (res?.status === 200) isAuthenticated = true;
  };

  const clearLocalStorage = (bypassClearForm = false) => {
    localStorage.removeItem("completed");
    localStorage.removeItem("issueTypeId");
    localStorage.removeItem("issueAddress");
    localStorage.removeItem("issueTime");
    localStorage.removeItem("issueDetailListCode");
    localStorage.removeItem("issueDetail");
    localStorage.removeItem("issueDescription");
    if (!bypassClearForm) clearForm();
  };

  const toggleDetails = (service_request_id) => {
    if (visibleDetails.has(service_request_id)) {
      visibleDetails.delete(service_request_id);

      setTimeout(() => {
        calculateBoundsAroundMarkers();
      }, 100);
    } else {
      visibleDetails.clear();
      visibleDetails.add(service_request_id);
    }
    visibleDetails = new Set(visibleDetails);

    scrollToIssue(service_request_id);
  };

  const applyFontStretch = () => {
    const style = document.createElement("style");
    style.textContent = `
        * {
          font-family: 'Roboto', 'Helvetica';
          letter-spacing: 0.12rem;
        }

        td, th, select, input, label, textarea, li, #success-message, #issue-details, .step-five-issue-description, .step-five-submitter-name, .step-five-contact-info, .step-five-issue-location-address, .step-five-issue-type, .success-message, .success-message-two-offline, #success-message-2 {
          letter-spacing: 0 !important;
        }

        .back-button {
          letter-spacing: 0.12rem !important;
        }
      `;

    document.head.appendChild(style);
  };

  const clearUploadMessages = () => {
    messageSuccess = "";
    messageRejectedOne = "";
    messageRejectedTwo = "";
  };

  const handleFileChange = async (event) => {
    clearUploadMessages();
    selectedFile = event.target.files[0];
    spinner = true;
    selectedFileSrc = await readFileAsDataURL(selectedFile);

    await handleSubmit();
  };

  const handleSubmit = async () => {
    // Convert the selected image file to a data URL
    imageData = await new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (event) => resolve(event.target.result);
      reader.readAsDataURL(selectedFile);
    });

    // Call the detectExplicitContent function with the image data URL
    let isExplicit;
    try {
      isExplicit = await detectExplicitContent(imageData, apiKey);
    } catch (err) {
      console.error(err);
      imageData = null;
      selectedFileSrc = null;
      selectedFile = null;
      spinner = false;
    }

    if (isExplicit) {
      messageRejectedOne =
        messages["report.issue"]["uploaded.message.rejected-one"];
      messageRejectedTwo =
        messages["report.issue"]["uploaded.message.rejected-two"];

      imageData = null;
      selectedFileSrc = null;
      selectedFile = null;
      spinner = false;
    } else if (
      messageRejectedOne !== messages["report.issue"]["vision.api.error"]
    ) {
      messageSuccess = messages["report.issue"]["uploaded.message.success"];
      spinner = false;
    }
  };

  const postImage = async (recaptchaToken) => {
    return new Promise(async (resolve, reject) => {
      try {
        const res = await axios.post("/image", {
          g_recaptcha_response: recaptchaToken,
          image: imageData,
        });

        mediaUrl = res.data;
        resolve();
      } catch (err) {
        reject(err);
      }
    });
  };

  const executeRecaptchaAndPostIssue = async () => {
    try {
      await new Promise((resolve, reject) => {
        recaptcha.renderRecaptcha((recaptchaToken) => {
          postImage(recaptchaToken).then(resolve).catch(reject);
        });
      });
      await postIssue();
    } catch (error) {
      console.error(error);
      postingError = true;

      clearLocalStorage();
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
      messageRejectedOne = messages["report.issue"]["vision.api.error"];
      messageRejectedTwo = messages["report.issue"]["vision.api.error.two"];

      spinner = false;
      imageData = null;
      selectedFileSrc = null;
      selectedFile = null;
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
    defaultOption.text = messages["find.issue"]["issue.type.placeholder"];
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
    multiSelectOptions = [];

    for (let i = 0; i < $issueDetailList.values.length; i++) {
      let obj = {
        label: $issueDetailList.values[i].name,
        value: $issueDetailList.values[i].key,
      };
      multiSelectOptions.push(obj);
    }
    // Reactive statement for arrays
    multiSelectOptions = multiSelectOptions;
  };

  const getIssues = async (
    page = 0,
    displayIssuesInMap = false,
    issuePosted = false
  ) => {
    let res;

    res = await axios.get(
      `/requests?page_size=${$itemsPerPage}&page=${page}&service_code=${filterIssueType.service_code}&start_date=${filterStartDate}&end_date=${filterEndDate}`
    );

    if (
      !issuePosted &&
      res.data?.length > 0 &&
      JSON.stringify(issuesData) !== JSON.stringify(res.data)
    ) {
      if (issuesData?.length > 0) issuesData = [...issuesData, ...res.data];
      else issuesData = [...res.data];
    } else issuesData = [...res.data];

    filteredIssuesData = issuesData;

    totalSize.set(res.headers["page-totalsize"]);
    totalPages.set(res.headers["page-totalpages"]);
    currentPage.set(res.headers["page-pagenumber"]);

    if (res.data && res.data?.length < Number($itemsPerPage)) {
      hasMoreResults = false;
    }

    if (res.data?.length > 0) {
      if (displayIssuesInMap) await addIssuesToMap();
    }
  };

  const validateOfflineAddress = (input) => {
    if (input.match(offlineAddressRegex)) invalidOfflineAddress = false;
    else {
      invalidOfflineAddress = true;
    }
  }

  const validateSubmitterName = (input) => {
    if (input.match(submitterNameRegex)) invalidSubmitterName.visible = false;
    else {
      invalidSubmitterName.visible = true;
    }
  }

  const validateEmail = (input) => {
    if (input.match(emailRegex)) invalidEmail.visible = false;
    else {
      invalidEmail.visible = true;
    }
  };

  const postIssue = async () => {
    let attributes = `?service_code=${
      $issueType.id
    }&address_string=${$issueAddress}&lat=${
      isOnline ? $issueAddressCoordinates.lat : ""
    }&long=${isOnline ? $issueAddressCoordinates.lng : ""}`;

    $issueDetail.forEach((attr) => {
      attributes += "&attribute[" + $issueDetailList.code + "]=" + attr.value;
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

    if (mediaUrl) attributes += "&media_url=" + mediaUrl;

    attributes += "&g_recaptcha_response=" + token;

    const data = new URLSearchParams(attributes);

    try {
      await axios.post("/requests.json", data, {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      });

      if (postingOfflineIssue) notifyOfflineIssuePosted = true;

      setTimeout(async () => {
        clearLocalStorage();
        issuesData = [];
        filteredIssuesData = [];
        token = null;
      }, 2000);
    } catch (err) {
      console.error(err);
    }

    // Creates a new marker for a newly reported issue
    if (isOnline) {
      if (provider === "osm"){
        const marker = new L.marker(
          [parseFloat($issueAddressCoordinates.lat), parseFloat($issueAddressCoordinates.lng)], {
            icon: L.icon({
              iconUrl: issuePinSVG,
              iconSize: [25, 25],
              iconAnchor: [12, 12]
            })
          }).addTo(map);
      }
      else if (provider === "googleMaps") {
        const marker = new google.maps.Marker({
          position: {
            lat: parseFloat($issueAddressCoordinates.lat),
            lng: parseFloat($issueAddressCoordinates.lng),
          },
          map: map,
          title: $issueType.name,
          icon: {
            scaledSize: new google.maps.Size(25, 25),
            url: issuePinSVG,
            anchor: new google.maps.Point(12, 12),
          },
        });

        markers.push(marker);
      }
    }
  };

  const postOfflineIssue = async () => {
    if (!localStorage.getItem("completed")) return;

    issueType.set({ id: localStorage.getItem("issueTypeId") });
    issueAddress.set(localStorage.getItem("issueAddress"));
    try {
      await geocoder.geocode({ address: $issueAddress }, (results, status) => {
        if (status === "OK") {
          const lat = results[0].geometry.location.lat();
          const lng = results[0].geometry.location.lng();

          issueAddressCoordinates.set({ lat: lat, lng: lng });
        } else {
          geocodeError = true;
          clearLocalStorage();
          clearForm();
          console.error(
            `Geocode was not successful for the following reason: ${status}`
          );
          return;
        }
      });
    } catch (err) {
      console.error(err);
      return;
    }

    issueDetailList.set({ code: localStorage.getItem("issueDetailListCode") });

    issueDetail.set(JSON.parse(localStorage.getItem("issueDetail")));

    if (localStorage.getItem("issueDescription"))
      issueDescription.set(localStorage.getItem("issueDescription"));

    if (localStorage.getItem("issueSubmitterName"))
      issueSubmitterName.set(localStorage.getItem("issueSubmitterName"));

    if (localStorage.getItem("issueSubmitterContact"))
      issueSubmitterContact.set(localStorage.getItem("issueSubmitterContact"));

    setTimeout(async () => {
      postingOfflineIssue = true;
      await postIssue();
    }, 5000);
  };

  const clearData = () => {
    issuesData = [];
    filteredIssuesData = [];
    totalPages.set(0);
    currentPage.set(0);
    hasMoreResults = true;
    clearMarkers();
    clearHeatmap();
  };

  const clearHeatmap = () => {
    if (heatmap) {
      if (provider === "googleMaps") {
        heatmap.setMap(null);
        heatmapData = [];
        heatmap.setData(heatmapData);
      }
      else if (provider === "osm") {
        map.removeLayer(heatmap);
        heatmapData = [];
      }
      
    }
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
    if (provider === "googleMaps") {
      geocoder.geocode({ location: latlng }, (results, status) => {
        if (status === "OK") {
          if (results[0]) {
            issueAddress.set(results[0].formatted_address);
            issueAddressCoordinates.set({ lat: lat, lng: lng });
            inputIssueAddressSelector.value = results[0].formatted_address;
          } else {
            console.log(messages["geocode"]["empty.results"]);
          }
        } else {
          console.log(`${messages["geocode"]["error"]} ${status}`);
        }
      });
    }
    else if (provider === "osm") {
      
    }
    
  };

  const setNewCenter = (lat, lng, zoom = 15) => {
    if (provider === "osm"){
      map.setView([lat, lng], zoom);
    }
    else if (provider === "googleMaps") {
      let newCenter = new google.maps.LatLng(lat, lng);
      map.setCenter(newCenter);
      setNewZoom(zoom);
    }
  }

  const setNewZoom = (zoomLevel) => {
    // if (provider === "osm") map.zoom(zoomLevel);
    if (provider === "googleMaps") map.setZoom(zoomLevel);
  };

  const successCallback = (position) => {
    $userCurrentLocation = {
      lat: position.coords.latitude ?? "38.95180510457306",
      lng: position.coords.longitude ?? "-92.32740864543621",
    };

    setNewCenter($userCurrentLocation.lat, $userCurrentLocation.lng);
    issueTime.set(convertDate(new Date()));
    if (provider === "googleMaps") geocodeLatLng($userCurrentLocation.lat, $userCurrentLocation.lng);
  };

  const errorCallback = (error) => {
    console.log(error);
  };

  // TODO
  const clearMarkers = () => {
    if (provider === "googleMaps") {
      markers.forEach((marker) => {
        marker.setMap(null);
      });
      markers = [];
    }
    else if (provider === "osm"){
      markers.forEach((marker) => {
        marker.remove()
      });
      markers = [];
    }
  };

  const clearIcons = () => {
    if (provider === "googleMaps") {
      markers.forEach((mkr) => {
        const icon = mkr.getIcon();
        icon.url = issuePinSVG;
        mkr.setIcon(icon);
      });
    }
    else if (provider === "osm") {
      markers.forEach((mkr) => {
        const icon = mkr.getIcon();
        icon.options.iconUrl = issuePinSVG;
        mkr.setIcon(icon);
      })
    }
  };

  // From Unix Epoch to Current Time
  const convertDate = (unixTimestamp) => {
    const date = new Date(unixTimestamp);
    return date.toLocaleString();
  };

  const resetState = () => {
    // setTimeout(() => scrollToTop(), 100);
    scrollToTop();
    showFooter = true;
    adjustFooter();
    reduceBackGroundOpacity = true;
    clearMarkers();
    issueAddress.set();
    issueDescription.set();
    issueSubmitterContact.set();
    issueSubmitterName.set();
    $issueDetail = [];
    $issueType = null;
    $issueDetailList = null;
    if (inputIssueAddressSelector) inputIssueAddressSelector.value = "";
    selectedFile = null;
    clearUploadMessages();
    invalidSubmitterName.visible = false;
    invalidEmail.visible = false;
    mediaUrl = undefined;
    imageData = "";
    selectedFileSrc = "";
    selectedFile = "";
    setTimeout(async () => {
      currentStep = null;
      showFooter = true;
      await adjustFooter();
    }, 700);
  };

  const clearForm = () => {
    issueAddress.set();
    issueDescription.set();
    issueSubmitterContact.set();
    issueSubmitterName.set();
    $issueDetail = [];
    $issueType = null;
    $issueDetailList = null;
    if (inputIssueAddressSelector) inputIssueAddressSelector.value = "";
    selectedFile = null;
    clearUploadMessages();
    invalidSubmitterName.visible = false;
    invalidEmail.visible = false;
    mediaUrl = undefined;
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

    if (heatmapVisible) {
      setTimeout(() => {
        const button = document.getElementById("Heatmap-control");
        if (button) button.innerHTML = messages["map"]["button.markers.label"];
      }, 200);
    }

    if (filteredIssuesData && filteredIssuesData.length > 0) {
      filteredIssuesData.forEach((issue) => {
        let marker;

        if (provider === "googleMaps") {
          marker = new google.maps.Marker({
            position: {
              lat: parseFloat(issue.lat),
              lng: parseFloat(issue.long),
            },
            map: map,
            title: issue.name,
            icon: {
              scaledSize: new google.maps.Size(25, 25),
              url:
                parseFloat(issue.lat) === selectedIssueMarker?.position.lat() &&
                parseFloat(issue.long) === selectedIssueMarker?.position.lng()
                  ? issuePinSelectedSVG
                  : issuePinSVG,
              anchor: new google.maps.Point(12, 12),
            },
          });
          markers.push(marker);
        } 
        else if (provider === "osm") {
          markerGroup = L.layerGroup().addTo(map);
          marker = new L.marker([parseFloat(issue.lat), parseFloat(issue.long)], {
            icon: L.icon({
              iconAnchor: [12, 12],
              iconSize: [25, 25],
              iconUrl:
                parseFloat(issue.lat) === selectedIssueMarker?.getLatLng().lat &&
                parseFloat(issue.long) === selectedIssueMarker?.getLatLng().lng
                  ? issuePinSelectedSVG
                  : issuePinSVG,
            }),
            title: issue.name,
          }).addTo(markerGroup);
          markers.push(marker);
        }
        

        if (provider === "googleMaps") {
          google.maps.event.addListener(marker, "click", function () {
            // Marker being deselected
            const selection = marker.getIcon();
            if (selection.url === issuePinSelectedSVG) {
              clearIcons();
              toggleDetails(issue.service_request_id);
              selectedIssueMarker = undefined;
              selectedIssue = undefined;
              return;
            }

            // Marker being selected: selects all the markers in the same location
            const selectedMarkers = markers.filter(
              (mrk) =>
                mrk.position.lat() === marker.position.lat() &&
                mrk.position.lng() === marker.position.lng()
            );

            // Clears all the markers that are not selected
            selectedMarkers.forEach((selectedMarker) => {
              markers.forEach((mkr) => {
                if (
                  mkr.position.lat() !== selectedMarker.position.lat() &&
                  mkr.position.lng() !== selectedMarker.position.lng()
                ) {
                  let icon = mkr.getIcon();
                  icon.url = issuePinSVG;
                  mkr.setIcon(icon);
                }
              });

              let icon = selectedMarker.getIcon();
              if (icon.url === issuePinSVG) icon.url = issuePinSelectedSVG;
              else {
                icon.url = issuePinSVG;
                selectedIssueMarker = undefined;
              }

              selectedMarker.setIcon(icon);
            });

            toggleDetails(issue.service_request_id);
            selectedIssue = issue;
            selectedIssueMarker = marker;
            setNewCenter(issue.lat, issue.long, 17);

            // Table
            const selectedRow = document.getElementById(issue.service_request_id);
            const rowIndex = Array.from(tableSelector.rows).indexOf(selectedRow);
            if (rowIndex > 0) {
              const rowAboveC = tableSelector.rows[rowIndex - 1];
              setTimeout(() => {
                rowAboveC.scrollIntoView({
                  behavior: "smooth",
                  block: "start",
                });
              }, 500);
            }
          });
        } else if (provider === "osm") {
          marker.on('click', function() {
            // Marker being deselected
            const selection = marker.getIcon();
            if (selection.options.iconUrl === issuePinSelectedSVG) {
              clearIcons();
              toggleDetails(issue.service_request_id);
              selectedIssueMarker = undefined;
              selectedIssue = undefined;
              return;
            }

            // Marker being selected: selects all the markers in the same location
            const selectedMarkers = markers.filter(
              (mrk) =>
                mrk.getLatLng().lat === marker.getLatLng().lat &&
                mrk.getLatLng().lng === marker.getLatLng().lng
                // mrk.position.lat() === marker.position.lat() &&
                // mrk.position.lng() === marker.position.lng()
            );

            // Clears all the markers that are not selected
            selectedMarkers.forEach((selectedMarker) => {
              markers.forEach((mkr) => {
                if (
                  mkr.getLatLng().lat !== selectedMarker.getLatLng().lat &&
                  mkr.getLatLng().lng !== selectedMarker.getLatLng().lng
                ) {
                  let icon = mkr.getIcon();
                  icon.options.iconUrl = issuePinSVG;
                  mkr.setIcon(icon);
                }
              });

              let icon = selectedMarker.getIcon();
              if (icon.options.iconUrl === issuePinSVG) icon.options.iconUrl = issuePinSelectedSVG;
              else {
                icon.options.iconUrl = issuePinSVG;
                selectedIssueMarker = undefined;
              }

              selectedMarker.setIcon(icon);
            });

            toggleDetails(issue.service_request_id);
            selectedIssue = issue;
            selectedIssueMarker = marker;
            setNewCenter(issue.lat, issue.long, 17);

            // Table
            const selectedRow = document.getElementById(issue.service_request_id);
            const rowIndex = Array.from(tableSelector.rows).indexOf(selectedRow);
            if (rowIndex > 0) {
              const rowAboveC = tableSelector.rows[rowIndex - 1];
              setTimeout(() => {
                rowAboveC.scrollIntoView({
                  behavior: "smooth",
                  block: "start",
                });
              }, 500);
            }
          })
        }
        // End of click handler

        heatmapData.push(
          provider === "osm" ? new L.LatLng(parseFloat(issue.lat), parseFloat(issue.long))
          : new google.maps.LatLng(parseFloat(issue.lat), parseFloat(issue.long))
        );
      });

      if (provider === "googleMaps") {
        heatmap = new google.maps.visualization.HeatmapLayer({
          data: heatmapData,
        });
      } else if (provider === "osm" && !heatmapVisible) {
        heatmap = L.heatLayer(heatmapData, {
          radius: 15,
          blur: 15,
        });
      }
      
      if (provider === "googleMaps") {
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
              button.innerHTML = messages["map"]["button.markers.label"];

              heatmap.setMap(map);
            } else {
              const button = document.getElementById("Heatmap-control");
              button.innerHTML = messages["map"]["button.heatmap.label"];

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
          if (!selectedIssue) calculateBoundsAroundMarkers();
        }, 400);
      } 
    }
  };

  const heatmapToggle = () => {
    let CustomControl = L.Control.extend({
      options: {
        position: "topright",
      },

      onAdd: function (map) {
        let container = L.DomUtil.create(
          "img",
          "leaflet-bar leaflet-control leaflet-control-custom"
        );

        container.style.backgroundColor = "white";
        container.src = fireSVG;
        container.style.width = "30px";
        container.style.height = "30px";
        container.innerHTML =
          '<button style="width: 100%; height: 100%;">X</button>';

        container.onclick = function () {

          heatmapVisible = !heatmapVisible;

          if (heatmapVisible) {     
            for (var i = 0; i < markers.length; i++) {
              markers[i].removeFrom(map);
            }    
            map.addLayer(heatmap);
            
          }
          if (!heatmapVisible) {
            map.removeLayer(heatmap);
            for (var i = 0; i < markers.length; i++) {
              markers[i].addTo(map);
            }
          }
        };

        return container;
      },
    });

    map.addControl(new CustomControl());
  };

  const calculateBoundsAroundMarkers = () => {
    if (provider === "osm") {
      if (markers && bounds) {
        let bounds = L.latLngBounds();
        
        markers.forEach(function (marker) {
          bounds.extend(marker.getLatLng());
        })
    
        map.fitBounds(bounds);
      }
    }
    else if (provider === "googleMaps") {
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
    const message = messages["home"]["leave.message"];

    event.preventDefault();
    event.returnValue = message;
    return message;
  };

  const openInNewWindow = (url) => {
    window.open(url, "_blank");
  };

  // TODO for OSM
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
    controlButton.style.marginBottom = "1rem";
    controlButton.style.marginLeft = "0.3rem";
    controlButton.style.width = "fit-content";
    controlButton.style.height = "20px";
    controlButton.innerText = text;
    if (window.innerWidth < 320) controlButton.style.fontSize = "0.65rem";

    controlButton.addEventListener("click", clickHandler);

    return controlButton;
  };

  const adjustSeeMore = () => {
    if (seeMore) {
      const seeMoreDiv = document.getElementById("see-more");
      seeMoreHeight.set(seeMoreDiv.offsetHeight + 10);
      backgroundSelector.style.height =
        Number(
          backgroundSelector.style.height.slice(
            0,
            backgroundSelector.style.height.length - 2
          )
        ) +
        $seeMoreHeight +
        "px";
    } else {
      backgroundSelector.style.height =
        Number(
          backgroundSelector.style.height.slice(
            0,
            backgroundSelector.style.height.length - 2
          )
        ) -
        $seeMoreHeight +
        "px";
    }
  };

  const adjustFooter = () => {
    return new Promise((resolve, reject) => {
      let retries = 0;

      if ($footerSelector) {
        const footerDivHeight =
          $footerSelector.offsetTop + $footerSelector.offsetHeight;
        backgroundSelector.style.height = footerDivHeight + "px";

        resolve();
      } else if (!$footerSelector && retries < 50) {
        retries++;
        setTimeout(() => adjustFooter(), 300);
      } else {
        reject(new Error(messages["home"]["footer.selector.error"]));
      }
    });
  };

  const adjustTable = () => {
    return new Promise((resolve, reject) => {
      let retries = 0;

      if (tableSelector && backgroundSelector) {
        const addExtra = 140;

        const tableHeight =
          tableSelector.offsetTop + tableSelector.offsetHeight;

        backgroundSelector.style.height = tableHeight + addExtra + "px";

        resolve();
      } else if (!tableSelector && retries < 50) {
        retries++;
        setTimeout(() => adjustTable(), 300);
      } else {
        reject(new Error(messages["home"]["table.selector.error"]));
      }
    });
  };

  const adjustMap = () => {
    const addExtra = 210;
    const mapSelector = document.getElementById("map");
    const mapHeight = mapSelector.offsetTop + mapSelector.offsetHeight;
    backgroundSelector.style.height = mapHeight + addExtra + "px";
  };

  const adjustOffline = () => {
    backgroundSelector.style.height =
      Number(
        backgroundSelector.style.height.slice(
          0,
          backgroundSelector.style.height.length - 2
        )
      ) +
      300 +
      "px";
  };

  const truncateStringMiddle = (str, maxLength) => {
    if (str.length <= maxLength) {
      return str;
    }

    const halfLength = (maxLength - 3) / 2;
    const start = str.slice(0, Math.ceil(halfLength));
    const end = str.slice(-Math.floor(halfLength));

    return `${start}...${end}`;
  };

  const reportCSV = async () => {
    try {
      const res = await axios.get(
        `/requests/download?service_name=${filterIssueType.service_name}&start_date=${filterStartDate}&end_date=${filterEndDate}`,
        {
          headers: {
            "Content-Type": "text/csv",
          },
          responseType: "blob",
          withCredentials: true,
        }
      );

      const blob = new Blob([res.data], { type: "text/csv" });
      const blobUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      console.error(messages["find.issue"]["CSV.download.error"], err);
    }
  };

  const initRecaptcha = async () => {
    if (typeof grecaptcha === "undefined") {
      await new Promise((resolve) => {
        const checkRecaptcha = setInterval(() => {
          if (typeof grecaptcha !== "undefined") {
            clearInterval(checkRecaptcha);
            resolve();
          }
        }, 100);
      });
    }

    // Load Google Recaptcha in the background of the page
    grecaptcha.enterprise.ready(async () => {
      token = await grecaptcha.enterprise.execute(sitekey, {
        action: "homepage",
      });
    });
  };

  const loadRecaptcha = async () => {
    await new Promise((resolve) => {
      const script = document.createElement("script");
      script.src = `https://www.google.com/recaptcha/enterprise.js?render=${sitekey}`;
      script.async = true;
      script.defer = true;
      script.onload = resolve;
      document.head.appendChild(script);
    });

    await initRecaptcha();
  };

  const handleToken = async (recaptchaToken) => {
    if (recaptchaToken) token = recaptchaToken;
    reportNewIssueStep5 = false;

    if (imageData) {
      await executeRecaptchaAndPostIssue();
    } else await postIssue();

    currentStep = 6;
    reportNewIssueStep6 = true;
    localStorage.setItem("completed", "true");
  };

  const readFileAsDataURL = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => resolve(event.target.result);
      reader.onerror = (error) => reject(error);
      reader.readAsDataURL(file);
    });
  };

  const populateAddress = () => {
    if ($issueAddress && offlineAddressInputSelector) {
      offlineAddressInputSelector.value = $issueAddress;
    }
  };

  function createCenterAroundMeControl(controlText, clickHandler) {
    const controlDiv = document.createElement("div");
    controlDiv.className = "centerAroundMeControl";
    controlDiv.addEventListener("click", clickHandler);
    controlDiv.title = controlText;
    return controlDiv;
  }

  const scrollToIssue = (id) => {
    if (issuesRefs[id]) {
      issuesRefs[id].scrollIntoView({ behavior: "smooth", block: "center" });
    }
  };

  const initOSM = async () => {
    const L = await import("leaflet");
    const GeoSearch = await import("leaflet-geosearch");
    await import ("leaflet.locatecontrol");
    await import("leaflet.heat");

    let reverseGeocodedAddress;

    inputIssueAddressSelector = document.getElementById("pac-input");

    const mapLayer = L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    });

    map = new L.map('map', {
      center: [38.6740015313782, -90.453269188364],
      layers: [mapLayer],
      zoom: zoom
    });

    // ~ leaflet-geosearch ~
    const provider = new GeoSearch.OpenStreetMapProvider();

    const searchControl = new GeoSearch.GeoSearchControl({
      provider: provider,
      autoComplete: true,
      autoCompleteDelay: 25,
      style: 'bar',
    });

    const icon = L.icon({
      iconAnchor: [34,68],
      iconSize: [71, 71],
      iconUrl: currentLocationSVG
    });

    currentPositionMarker = new L.marker(map.getCenter(), {
      icon: icon,
      title: messages["map"]["marker.title"]
    }).addTo(map)

    function searchEventHandler(result) {
      console.log(result.location)
      issueAddress.set(result.location.label)
      issueAddressCoordinates.set({lat: result.location.y, lng: result.location.x})
    }

    function centerMarkerOnMap(map) {
      if (!findReportedIssue) {
        currentPositionMarker.setLatLng(map.target.getCenter());
      }
    }

    function geocodeFromMarker() {
      if (!findReportedIssue) {
        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${currentPositionMarker.getLatLng().lat}&lon=${currentPositionMarker.getLatLng().lng}`)
          .then((response) => response.json())
          .then((data) => {
            reverseGeocodedAddress = data.display_name;
          })
        
        issueAddress.set(reverseGeocodedAddress);
        issueAddressCoordinates.set({lat: currentPositionMarker.getLatLng().lat, lng: currentPositionMarker.getLatLng().lng})
      }
    }

    L.control.locate().addTo(map);
    map.addControl(searchControl);

    
    map.on('move', centerMarkerOnMap)
    map.on('moveend', geocodeFromMarker);
    map.on('geosearch/showlocation', searchEventHandler);

    heatmapToggle();
  }

  const initGoogleMaps = async () => {
    import("@googlemaps/js-api-loader").then((module) => {
      const Loader = module.Loader;

      const loader = new Loader({
        apiKey,
        version: "weekly",
        libraries: ["places", "visualization"],
      });

      loader.load().then(async (google) => {
        map = new google.maps.Map(document.getElementById("map"), {
          zoom: zoom,
          center: { lat: 38.6740015313782, lng: -90.453269188364 },
          mapTypeControl: false,
          gestureHandling: "greedy",
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

        const centerAroundMeControl = createCenterAroundMeControl(
          "Center Around Me",
          function () {
            navigator.geolocation.getCurrentPosition(
              successCallback,
              errorCallback,
              {
                enableHighAccuracy: true,
              }
            );
          }
        );

        map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(mapControl);
        map.controls[google.maps.ControlPosition.BOTTOM_LEFT].push(
          satelliteControl
        );
        map.controls[google.maps.ControlPosition.TOP_LEFT].push(
          inputIssueAddressSelector
        );
        map.controls[google.maps.ControlPosition.LEFT_TOP].push(
          centerAroundMeControl
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

            setNewCenter(
              place.geometry.location.lat(),
              place.geometry.location.lng(),
              17
            );
          });
        });

        if (localStorage.getItem("completed")) await postOfflineIssue();
      });
    });
  };

  const updateOnlineStatus = async () => {
    wasOnline = isOnline;
    isOnline = navigator.onLine;

    if (wasOnline && !isOnline) {
      // We are now offline
      if (findReportedIssue) resetFindIssue();

      if (
        reportNewIssue ||
        reportNewIssueStep2 ||
        reportNewIssueStep3 ||
        reportNewIssueStep4 ||
        reportNewIssueStep5
      )
        resetReportIssue();

      setTimeout(() => scrollToTop(), 500);

      reduceBackGroundOpacity = true;
    } else if (!wasOnline && isOnline) {
      // We are back online

      if (findReportedIssue) resetFindIssue();
      if (
        reportNewIssue ||
        reportNewIssueStep2 ||
        reportNewIssueStep3 ||
        reportNewIssueStep4 ||
        reportNewIssueStep5
      )
        resetReportIssue();

      setTimeout(() => scrollToTop(), 500);

      await initGoogleMaps();

      reduceBackGroundOpacity = true;
    }
  };

  const resetReportIssue = () => {
    if (reportNewIssue) reportNewIssue = false;
    if (reportNewIssueStep2) reportNewIssueStep2 = false;
    if (reportNewIssueStep3) reportNewIssueStep3 = false;
    if (reportNewIssueStep4) reportNewIssueStep4 = false;
    if (reportNewIssueStep5) reportNewIssueStep5 = false;
    if (reportNewIssueStep6) reportNewIssueStep6 = false;

    currentStep = null;
    scrollToTop();
    showFooter = true;
    adjustFooter();
  };

  const selectIssue = (issue) => {
    if (provider === "googleMaps"){
      toggleDetails(issue.service_request_id);
      selectedIssue = issue;

      // In the case there are more than one marker stacked in the same coordinate
      const selectedMarkers = markers.filter(
        (mrk) =>
          mrk.position.lat() === Number(issue.lat) &&
          mrk.position.lng() === Number(issue.long)
      );

      if (selectedMarkers) {
        selectedIssueMarker = selectedMarkers[0];

        selectedMarkers.forEach((selectedMarker) => {
          markers.forEach((mkr) => {
            if (
              mkr.position.lat() === selectedMarker.position.lat() &&
              mkr.position.lng() === selectedMarker.position.lng()
            ) {
              let icon = mkr.getIcon();
              icon.url = issuePinSelectedSVG;
              mkr.setIcon(icon);
            }
          });
        });
      }
    }
    else if (provider === "osm") {
      toggleDetails(issue.service_request_id);
      selectedIssue = issue;

      const selectedMarkers = markers.filter(
        (mrk) =>
          mrk.getLatLng().lat === Number(issue.lat) &&
          mrk.getLatLng().lng === Number(issue.long)
      );

      if (selectedMarkers) {
        selectedIssueMarker = selectedMarkers[0];

        selectedMarkers.forEach((selectedMarker) => {
          markers.forEach((mkr) => {
            if (
              mkr.getLatLng().lat === selectedMarker.getLatLng().lat &&
              mkr.getLatLng().lng === selectedMarker.getLatLng().lng
            ) {
              let icon = mkr.getIcon();
              icon.options.iconUrl = issuePinSelectedSVG;
              mkr.setIcon(icon);
            }
          })
        })
      }
    }
    
  };

  const deselectIssue = (issue) => {
    toggleDetails(issue);
    clearIcons();
    selectedIssue = undefined;
    selectedIssueMarker = undefined;
  };

  const resetFindIssue = () => {
    token = null;
    scrollToTop();
    adjustFooter();
    showFooter = true;
    

    findReportedIssue = false;
    showFilters = false;

    setTimeout(async () => {
      await adjustFooter();
    }, 600);

    clearData();
    clearFilters();

    if (selectedIssue) toggleDetails(selectedIssue.service_request_id);
    selectedIssue = null;
  };

  const handleResize = () => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      if (findReportedIssue) {
        adjustTable();
      }

      if (showFooter) {
        adjustFooter();
      }
    }, debounceTime);
  };

  onMount(async () => {
    setTimeout(() => console.clear(), 100);
    await loadRecaptcha();
    await getTokenInfo();

    isOnline = navigator.onLine;

    // Warn user before leaving the website
    window.addEventListener("beforeunload", handleBeforeUnload);

    // Listen for Device Orientation changes
    window.addEventListener("orientationchange", getOrientation);

    window.addEventListener("online", updateOnlineStatus);
    window.addEventListener("offline", updateOnlineStatus);

    window.addEventListener("resize", handleResize);

    getOrientation();

    loadColorPalette();

    let font = new FontFaceObserver("Gotham");

    font
      .load()
      .then(function () {
        // font is loaded successfully.
      })
      .catch(function () {
        // font failed to load.
        applyFontStretch();
      });

    

    await getAllServiceCodes();

    // Trigger the Svelte Transitions
    fadeInBackground = true;
    openLogo = true;

    // if (isOnline) await initOSM();
    // if (isOnline) await initGoogleMaps();
    if (mapProvider["provider"] === "googleMaps") {
      await initGoogleMaps();
    } else if (mapProvider["provider"] === "osm") {
      await initOSM();
    }

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
    height="{pageHeight}px"
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
    </div>

    {#if notifyOfflineIssuePosted}
      <Modal
        title="{messages['report.issue']['modal.offline.issue.posted.title']}"
        color="{primaryOne}"
        on:cancel="{() => (notifyOfflineIssuePosted = false)}"
      >
        <div class="reported-offline-issue-success">
          {messages["report.issue"]["modal.offline.issue.posted.description"]}
        </div>
      </Modal>
    {/if}

    {#if geocodeError}
      <Modal
        title="{messages['report.issue'][
          'modal.offline.issue.posted.title.error'
        ]}"
        color="{primaryOne}"
        on:cancel="{() => (geocodeError = false)}"
      >
        <div class="reported-offline-issue-success">
          {messages["report.issue"][
            "modal.offline.issue.posted.description.error"
          ]}
        </div>
      </Modal>
    {/if}

    <div
      class="content"
      in:fade="{{ delay: startRendering, duration: 1000, quintOut }}"
      out:fade="{{ duration: 300, quintOut }}"
    >
      <div class="slogan-title">{messages["home"]["tagline.one"]}</div>

      <div class="slogan-text">
        {messages["home"]["tagline.two"]}

        <!-- svelte-ignore a11y-click-events-have-key-events -->
        <span
          class="see-more"
          on:click="{() => {
            seeMore = !seeMore;
            setTimeout(() => {
              adjustSeeMore();
            }, 50);
          }}"
        >
          {#if seeMore}
            less...
          {:else}
            more...
          {/if}
        </span>

        {#if seeMore}
          <div id="see-more">
            <div class="see-more-title">
              {messages["home"]["see.more.title"]}
            </div>
            <div class="see-more-description">
              {messages["home"]["see.more.description"]}
            </div>
          </div>
        {/if}
      </div>

      <div class="action-buttons">
        {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5}
          <button
            bind:this="{findIssuesButtonSelector}"
            class="button"
            class:button-find-issue-disabled="{showModal || !isOnline}"
            disabled="{showModal || !isOnline}"
            id="button-find-issues"
            on:click="{async () => {
              if (reportNewIssueStep6 || !isOnline) return;

              // Clears the current position marker from the map
              if (provider === "googleMaps") {
                currentPositionMarker.setMap(null);
              }

              if (provider === "osm") {
                currentPositionMarker.remove();
              }
              
              // Clears the value of the input field inside the map
              inputIssueAddressSelector = document.getElementById('pac-input');
              inputIssueAddressSelector.value = '';

              if (!findReportedIssue) {
                showFooter = false;
                findReportedIssue = true;
                setTimeout(async () => {
                  await adjustTable();

                  setTimeout(() => {
                    findIssuesButtonSelector.scrollIntoView({
                      behavior: 'smooth',
                      block: 'start',
                    });
                  }, 650);
                }, 250);

                if (filteredIssuesData?.length === 0) await getIssues();

                addIssuesToMap();
              } else resetFindIssue();
            }}"
          >
            {#if !findReportedIssue}
              <img
                src="{searchSVG}"
                alt="search for reported issues"
                class="search-svg"
                height="23rem"
              />
            {:else}
              <img
                src="{closeSVG}"
                alt="close find an issue"
                style="vertical-align: -0.3rem; margin-right: {window.innerWidth >
                320
                  ? '1.3rem'
                  : '1rem'}; margin-left: -0.7rem"
                height="{window.innerWidth < 320 ? '20rem' : '25rem'}"
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
              if (!localStorage.getItem('completed')) clearLocalStorage(true);
              if (reportNewIssueStep6) return;

              if (isOnline) {
                if (provider === "googleMaps"){
                  if (map.controls[window.google.maps.ControlPosition.BOTTOM_LEFT].length === 3) 
                    map.controls[window.google.maps.ControlPosition.BOTTOM_LEFT].removeAt(heatmapControlIndex);

                  currentPositionMarker.setMap(map);
                }
                else if (provider === "osm") {
                  currentPositionMarker.addTo(map);
                }
                
              } else {
                const stepOneDiv = document.getElementById('stepOne');

                if (
                  window.innerWidth >= 320 &&
                  window.innerWidth <= 374 &&
                  screen.orientation.type.includes('portrait')
                ) {
                  if (stepOneDiv) stepOneDiv.style.height = '16rem';
                } else if (
                  window.innerWidth >= 596 &&
                  window.innerWidth <= 814 &&
                  screen.orientation.type.includes('portrait')
                ) {
                  if (stepOneDiv) stepOneDiv.style.height = '15.5rem';
                } else if (
                  window.innerWidth >= 375 &&
                  screen.orientation.type.includes('portrait')
                ) {
                  if (stepOneDiv) stepOneDiv.style.height = '14.5rem';
                } else {
                  if (stepOneDiv) stepOneDiv.style.height = '18rem';
                }
              }

              if (!reportNewIssue && !currentStep) {
                showFooter = false;
                reportNewIssue = true;
                setTimeout(() => {
                  if (isOnline) adjustMap();
                  else {
                    adjustOffline();
                    setTimeout(() => populateAddress(), 100);
                  }

                  setTimeout(() => {
                    reportIssuesButtonSelector.scrollIntoView({
                      behavior: 'smooth',
                      block: 'start',
                    });
                  }, 500);
                }, 100);

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
              if (isOnline) {
                navigator.geolocation.getCurrentPosition(
                  successCallback,
                  errorCallback,
                  {
                    enableHighAccuracy: true,
                  }
                );
              }
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
                style="vertical-align: -0.3rem; margin-right: {window.innerWidth >
                320
                  ? '1.3rem'
                  : '1rem'}; margin-left: -2.1rem"
                height="{window.innerWidth < 320 ? '20rem' : '25rem'}"
              />
            {/if}
            {messages["home"]["button.report.issue.label"]}
          </button>
        {/if}
      </div>
    </div>
    {#if !reportNewIssue && !reportNewIssueStep2 && !reportNewIssueStep3 && !reportNewIssueStep4 && !reportNewIssueStep5 && !reportNewIssueStep6 && !findReportedIssue && showFooter}
      <Footer bind:this="{$footerSelector}" backgroundColor="{primaryOne}" />
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
              {$issueTime ?? convertDate(new Date().getTime())}
            </span>
          </span>
          <div
            class="describe-issue"
            style="text-shadow: 2px 2px 2px rgba(0, 0, 0, 0.8); font-size: 1.3rem"
          >
            <div class="step-two-feature-type-label">
              {messages["report.issue"]["label.feature.type"]}
            </div>
          </div>
          <div class="step-two-select-div">
            <select
              class="step-two-select"
              bind:this="{issueTypeSelectSelector}"
              on:change="{async (e) => {
                selected = [];

                if ($issueDetail) issueDetail.set([]);

                issueType.set({
                  id: e.target.value,
                  name: e.target.options[e.target.selectedIndex].text,
                });

                if ($issueType.name !== 'Other') {
                  await getServiceDefinition(e.target.value);

                  populateIssueDetailList();

                  setTimeout(() => {
                    let inputElement = document.querySelector('#issue-details');
                    if (inputElement)
                      inputElement.setAttribute('readonly', 'readonly');
                  }, 1000);
                }
              }}"></select>

            {#if $issueDetailList?.description && $issueType?.name !== "Other"}
              <div class="step-two-feature-type-helper">
                {$issueDetailList.description}
              </div>
            {:else if $issueType?.name === "Other"}
              <div class="step-two-feature-type-helper">
                {messages["report.issue"]["selection.other.description"]}
              </div>
            {/if}

            {#if $issueDetailList && $issueType.name !== "Other" && multiSelectOptions?.length > 0}
              <div class="multiselect">
                <MultiSelect
                  id="issue-details"
                  placeholder="Issue Details"
                  bind:selected="{selected}"
                  options="{multiSelectOptions}"
                  on:change="{() => issueDetail.set(selected)}"
                />
              </div>
            {/if}

            {#if $issueType !== null}
              <div class="textarea">
                {#if $issueDetail.find((selection) => selection.name === "Other") || $issueType.name === "Other"}
                  <div class="step-two-required">* Required field</div>
                {/if}

                <textarea
                  placeholder="{$issueDetail.find(
                    (selection) => selection.name === 'Other'
                  ) || $issueType.name === 'Other'
                    ? messages['report.issue'][
                        'textarea.description.placeholder'
                      ]
                    : messages['report.issue'][
                        'textarea.description.not.required.placeholder'
                      ]}"
                  rows="3"
                  maxlength="{maxCharactersLength}"
                  bind:value="{$issueDescription}"
                  on:click="{() => (invalidOtherDescription.visible = false)}"
                ></textarea>
              </div>

              <div class="step-two-word-count">
                <span
                  class:step-two-word-count-accent="{$issueDescription?.length <
                    10 &&
                    ($issueDetail.find(
                      (selection) => selection.name === 'Other'
                    ) ||
                      $issueType.name === 'Other')}"
                >
                  {$issueDescription?.length ?? 0}
                </span>
                /{maxCharactersLength}
              </div>

              <div
                class="step-two-word-count-error"
                class:visible="{invalidOtherDescription.visible}"
              >
                {invalidOtherDescription.message}
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
              ($issueDetail?.length < 1 && $issueType.name !== 'Other')}"
            disabled="{$issueType === null ||
              ($issueDetail?.length < 1 && $issueType.name !== 'Other')}"
            style="margin-bottom: 1.25rem"
            on:click="{() => {
              if (!isOnline) issueTime.set(convertDate(new Date()));

              localStorage.setItem('issueTime', $issueTime);
              localStorage.setItem('issueTypeId', $issueType.id);
              if ($issueDetail)
                localStorage.setItem(
                  'issueDetail',
                  JSON.stringify($issueDetail)
                );

              if ($issueType.name !== 'Other') {
                localStorage.setItem(
                  'issueDetailListCode',
                  $issueDetailList.code
                );
              }

              if ($issueDescription)
                localStorage.setItem('issueDescription', $issueDescription);

              if (
                (($issueType.name === 'Other' ||
                  $issueDetail.find(
                    (selection) => selection.name === 'Other'
                  )) &&
                  $issueDescription?.length < minOtherDescriptionLength) ||
                $issueDescription === null
              ) {
                invalidOtherDescription.visible = true;
              } else {
                reportNewIssueStep2 = false;
                currentStep = 3;
                reportNewIssueStep3 = true;
                setTimeout(() => {
                  reportIssuesButtonSelector.scrollIntoView();
                }, 100);
              }
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
            <span style="font-size: 1.2rem; margin-left: 0.3rem">
              {messages["report.issue"]["label.optional"]}
            </span>
          </div>
          <span class="step-three-add-media-label">
            {messages["report.issue"]["label.add.media"]}
          </span>

          <div style="margin-top: 1rem">
            <form
              data-sitekey="{sitekey}"
              data-callback="onSubmit"
              data-action="submit"
              class="g-recaptcha"
            >
              <div style="display: flex">
                <label
                  for="image-uploads"
                  class="upload-image"
                  id="upload-image-label"
                >
                  {#if selectedFile}
                    {truncateStringMiddle(
                      selectedFile.name,
                      window.innerWidth >= 320 && window.innerWidth < 375
                        ? 19
                        : 25
                    )}
                  {:else if isOnline}
                    {messages["report.issue"]["label.choose.image"]}
                  {:else if !isOnline}
                    {messages["report.issue"]["image.upload.disabled"]}
                  {/if}

                  <!-- svelte-ignore a11y-img-redundant-alt -->
                  <img
                    src="{cameraSVG}"
                    alt="take photo"
                    max-width="37rem"
                    max-height="37rem"
                    style="padding: 0 0.3rem 0 0.3rem; vertical-align: middle; background-color: white; border-radius: 10px; margin-left: 0.5rem; margin-right: 0.5rem; cursor: pointer"
                  />
                </label>
                <input
                  type="file"
                  id="image-uploads"
                  accept="image/*"
                  style="display: none; cursor: {isOnline
                    ? 'pointer'
                    : 'default'}"
                  disabled="{!isOnline}"
                  on:change="{(e) => {
                    if (isOnline) handleFileChange(e);
                    e.target.value = '';
                  }}"
                />

                <Recaptcha bind:this="{recaptcha}" sitekey="{sitekey}" />
              </div>
            </form>

            {#if spinner}
              <div class="loader">
                <span></span>
                <span></span>
                <span></span>
                <span></span>
              </div>
            {/if}

            {#if messageRejectedOne}
              <!-- svelte-ignore a11y-img-redundant-alt -->
              <img
                src="{forbiddenSVG}"
                alt="rejected image"
                height="auto"
                width="50rem"
                class="fobidden-icon"
              />
              <div class="upload-message">{messageRejectedOne}</div>
              <div class="upload-message">{messageRejectedTwo}</div>
            {:else if messageSuccess && imageData}
              <!-- svelte-ignore a11y-img-redundant-alt -->
              <div class="upload-message">
                <div class="image-container">
                  <img
                    src="{selectedFileSrc}"
                    alt="issue image"
                    height="auto"
                    width="75rem"
                    style="vertical-align: middle; margin-right: 0.5rem; border-radius: 10px"
                  />
                  <!-- svelte-ignore a11y-click-events-have-key-events -->
                  <div
                    class="remove-btn"
                    on:click="{(e) => {
                      e.target.parentElement.style.display = 'none';
                      selectedFile = null;
                      selectedFileSrc = null;
                      imageData = null;
                      messageSuccess = '';
                    }}"
                  >
                    X
                  </div>
                </div>
                <div style="margin-top: 1rem" id="success-message">
                  {messageSuccess}
                </div>
              </div>
            {/if}

            <button
              class="button back-button"
              style="margin-bottom: 1.25rem; margin-top: 2rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                reportNewIssueStep2 = true;
                currentStep = 2;
                setTimeout(() => populateIssueTypeSelectDropdown(), 10);

                setTimeout(() => {
                  if ($issueType !== null) {
                    issueTypeSelectSelector.value = $issueType.id;
                    populateIssueDetailList();
                  }
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
              class="button next-button"
              style="margin-bottom: 1.25rem; margin-top: 2rem"
              on:click="{() => {
                reportNewIssueStep3 = false;
                currentStep = 4;
                reportNewIssueStep4 = true;

                if (localStorage.getItem('issueSubmitterName'))
                  $issueSubmitterName =
                    localStorage.getItem('issueSubmitterName') ?? '';
                if (localStorage.getItem('issueSubmitterContact'))
                  $issueSubmitterContact =
                    localStorage.getItem('issueSubmitterContact') ?? '';
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
            <span style="font-size: 1.2rem; margin-left: 0.3rem">
              {messages["report.issue"]["label.optional"]}
            </span>
          </div>

          <div class="submitter-name-div">
            <span class="step-four-submitter-name-label">
              {messages["report.issue"]["label.submitter.name"]}
            </span>
            <input
              class="step-four-input-submitter-name"
              bind:value="{$issueSubmitterName}"
              on:blur="{() => {
                if ($issueSubmitterName) {
                  $issueSubmitterName = $issueSubmitterName.trim();
                }
              }}"
              on:click="{() => (invalidSubmitterName.visible = false)}"
              placeholder="{messages['report.issue'][
                'placeholder.submitter.name'
              ]}"
            />

            <div
              class="step-four-submitter-name-word-count-error"
              class:visible="{invalidSubmitterName.visible}"
            >
              {invalidSubmitterName.message}
            </div>
          </div>

          <div class="contact-div">
            <span class="step-four-contact-info-label">
              {messages["report.issue"]["label.contact.info"]}
            </span>
            <input
              type="email"
              class="step-four-input-contact-info"
              bind:value="{$issueSubmitterContact}"
              on:blur="{() => {
                if ($issueSubmitterContact) {
                  $issueSubmitterContact = $issueSubmitterContact.trim();
                }
              }}"
              on:click="{() => (invalidEmail.visible = false)}"
              placeholder="{messages['report.issue'][
                'placeholder.contact.info'
              ]}"
            />

            <div
              class="step-four-submitter-email-error"
              class:visible="{invalidEmail.visible}"
            >
              {invalidEmail.message}
            </div>
          </div>

          <button
            class="button back-button"
            style="margin-bottom: 1.25rem"
            on:click="{() => {
              reportNewIssueStep4 = false;
              reportNewIssueStep3 = true;
              currentStep = 3;
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
              // Reviews data for the issue before it can be reported
              if (
                $issueSubmitterName?.length < minSubmitterNameLength &&
                $issueSubmitterName?.length !== 0
              ) {
                console.log("invalid submitter name")
                invalidSubmitterName.visible = true;
                return;
              }

              if ($issueAddress) validateOfflineAddress($issueAddress);
              if ($issueSubmitterName) validateSubmitterName($issueSubmitterName);
              if ($issueSubmitterContact) validateEmail($issueSubmitterContact);

              if (invalidOfflineAddress) return;
              if (invalidSubmitterName.visible) return;
              if (invalidEmail.visible) return;

              localStorage.setItem(
                'issueSubmitterName',
                $issueSubmitterName ?? ''
              );

              localStorage.setItem(
                'issueSubmitterContact',
                $issueSubmitterContact ?? ''
              );

              reportNewIssueStep4 = false;
              currentStep = 5;
              reportNewIssueStep5 = true;
            }}"
          >
            {#if window.innerWidth < 320}
              {messages["report.issue"]["button.review.submit.short"]}
            {:else}
              {messages["report.issue"]["button.review.submit"]}
            {/if}
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
                <span id="issue-details" style="margin-right: 1rem"
                  >{i + 1}-{detail.label}</span
                >
              {/each}
            </div>
          </div>

          {#if selectedFileSrc}
            <div class="step-five-issue-detail-label">
              {messages["report.issue"]["label.review.media"]}
            </div>
            <!-- svelte-ignore a11y-img-redundant-alt -->
            <img
              src="{selectedFileSrc}"
              height="auto"
              width="75rem"
              alt="uploaded image"
              class="image-review"
            />
          {/if}

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

          {#if mediaUrl && !$issueDescription && !$issueSubmitterName && !$issueSubmitterContact}
            <div></div>
          {/if}
          <Recaptcha bind:this="{recaptcha}" sitekey="{sitekey}" />

          <button
            class="button back-button"
            style="margin-bottom: 1.25rem; margin-top: 2rem"
            on:click="{() => {
              reportNewIssueStep5 = false;
              reportNewIssueStep4 = true;
              currentStep = 4;
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
            class="button submit-button g-recaptcha"
            class:next-button="{$issueType && $issueDetail}"
            class:disabled-button="{$issueType === null ||
              $issueDetail === null}"
            data-sitekey="{sitekey}"
            data-callback="handleToken"
            data-action="submit"
            on:click="{() => {
              if (isOnline) recaptcha.renderRecaptcha(handleToken);
              else handleToken('');
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
            {#if postingError}
              {messages["report.issue"]["issue.reported.failure"]}
            {:else}
              {messages["report.issue"]["issue.reported.success.message.one"]}
            {/if}
          </div>

          <div
            class:success-message-two-offline="{!isOnline}"
            style="margin-bottom: 0.5rem"
            id="success-message-2"
          >
            {#if isOnline && postingError === false}
              {messages["report.issue"]["issue.reported.success.message.two"]}
            {:else if isOnline === false && postingError === false}
              {messages["report.issue"][
                "issue.reported.success.message.two.offline"
              ]}
            {/if}
          </div>
        </div>
      </div>
    {/if}

    <!-- Step 1 goes at the end because it has to be loaded due to the map and is hidden -->

    <div class="find-issues">
      <div
        id="stepOne"
        class:visible="{reportNewIssue || findReportedIssue}"
        style="width:{(!isOnline && window.innerWidth) > 320
          ? '50vw'
          : '100vw'}"
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

            {#if isOnline}
              <div class="step-one-instruction">
                {messages["report.issue"]["label.issue.location.subtext"]}
              </div>
            {:else}
              <div class="offline-input">
                <input
                  bind:this="{offlineAddressInputSelector}"
                  class="offline-address-input"
                  placeholder="{messages['map']['pac-input-placeholder']}"
                  on:click="{() => (invalidOfflineAddress = false)}"
                />
              </div>

              <div
                class="step-one-invalid-offline-address"
                class:visible="{invalidOfflineAddress}"
              >
                {messages["report.issue"]["invalid.offline.address"]}
              </div>
            {/if}

            <div
              class="step-one-issue-address"
              style="display:{isOnline ? 'inherit' : 'none'}"
            >
              {#if isOnline}
                <span style="color: {primaryTwo}">
                  {$issueAddress ??
                    messages["report.issue"]["location.services.disabled"]}
                </span>
              {/if}
            </div>
            <div>
              <button
                class="button next-button"
                step-one-button-next="{$issueAddress}"
                class:disabled-button="{!$issueAddress && isOnline}"
                disabled="{!$issueAddress && isOnline}"
                style="margin-bottom: 0.5rem"
                on:click="{() => {
                  if (!isOnline) {
                    if (
                      offlineAddressInputSelector?.value?.length >=
                      minAddressCharacters
                    ) {
                      issueAddress.set(offlineAddressInputSelector.value);
                    } else {
                      invalidOfflineAddress = true;
                      return;
                    }
                  }

                  localStorage.setItem('issueAddress', $issueAddress); // for offline mode

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
        {#if isOnline}
          <input
            id="pac-input"
            placeholder="{messages['map']['pac-input-placeholder']}"
            type="text"
          />

          <div id="map"></div>
        {/if}
        <!-- START Find Reported Issue -->

        {#if showModal}
          <Modal
            title="Issue Details"
            color="{primaryOne}"
            on:cancel="{() => (showModal = false)}"
          >
            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >{messages["modal"]["label.type"]}</span
              >
              {selectedIssue.service_name}
            </div>

            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >{messages["modal"]["label.detail"]}</span
              >
              {#if selectedIssue?.selected_values}
                {#each selectedIssue.selected_values[0]?.values as issueDetail, i}
                  <span style="margin-right:1rem"
                    >{i + 1}-{issueDetail.name}</span
                  >
                {/each}
              {:else}
                -
              {/if}
            </div>

            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >{messages["modal"]["label.description"]}</span
              >{selectedIssue.description ?? "-"}
            </div>

            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >{messages["modal"]["label.requested.at"]}</span
              >{formatDate(selectedIssue.requested_datetime)}
            </div>

            <div class="issue-detail-line">
              <span style="font-weight: 300; margin-right: 0.3rem"
                >{messages["modal"]["label.location"]}</span
              >{selectedIssue.address}
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
            <div>
              {messages["find.issue"]["label.filter"]}
              {#if showFilters}
                <!-- svelte-ignore a11y-click-events-have-key-events -->
                <span
                  on:click="{() => {
                    showFilters = !showFilters;
                  }}"
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
                    setTimeout(() => {
                      populateIssueTypeSelectDropdown();
                    }, 10);
                  }}"
                >
                  +
                </span>
              {/if}
            </div>

            <!-- svelte-ignore a11y-click-events-have-key-events -->
            <div
              style="margin-right: 1rem; cursor: pointer"
              on:click="{reportCSV}"
            >
              {#if isAuthenticated}
                {messages["find.issue"]["label.download.csv"]}
              {/if}
            </div>
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
            <table bind:this="{tableSelector}" class="issues-table">
              <thead>
                <tr>
                  <th id="issue-type-header">
                    {messages["find.issue"]["issues.table.column.one"]}
                  </th>
                  <th>
                    {messages["find.issue"]["issues.table.column.two"]}
                  </th>
                  <th style="text-align: center">
                    {messages["find.issue"]["issues.table.column.three"]}
                  </th>
                  <th style="text-align: center">
                    {messages["find.issue"]["issues.table.column.four"]}
                  </th>
                </tr>
              </thead>

              <tbody>
                {#if filteredIssuesData}
                  {#each filteredIssuesData as issue (issue.service_request_id)}
                    <tr
                      id="{issue.service_request_id}"
                      bind:this="{issuesRefs[issue.service_request_id]}"
                      on:click="{(e) => {
                        const clickedElement = e.target;
                        const selectedRow = clickedElement.closest('tr');
                        const rowIndex = Array.from(tableSelector.rows).indexOf(
                          selectedRow
                        );

                        if (rowIndex > 0) {
                          const rowAbove = tableSelector.rows[rowIndex - 1];
                          rowAbove.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start',
                          });
                        }

                        setNewCenter(issue.lat, issue.long, 17);
                      }}"
                      style="background-color: {visibleDetails.has(
                        issue.service_request_id
                      )
                        ? hexToRGBA(secondaryTwo, 0.1)
                        : 'white'}
                      "
                    >
                      <!-- svelte-ignore a11y-click-events-have-key-events -->
                      <td
                        id="td-issue-type"
                        on:click="{() => {
                          if (
                            selectedIssue &&
                            selectedIssue.lat === issue.lat &&
                            selectedIssue.long === issue.long
                          ) {
                            deselectIssue(issue.service_request_id);
                            return;
                          } else {
                            clearIcons();
                            selectIssue(issue);
                          }
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
                        id="td-description"
                        style="height: {visibleDetails.has(
                          issue.service_request_id
                        )
                          ? '4rem'
                          : '1rem'};
                          
                          white-space: {visibleDetails.has(
                          issue.service_request_id
                        )
                          ? 'normal'
                          : 'nowrap'};
                          
                          overflow: {visibleDetails.has(
                          issue.service_request_id
                        )
                          ? 'unset'
                          : 'hidden'}; 
                          "
                        on:click="{() => {
                          if (
                            selectedIssue &&
                            selectedIssue.lat === issue.lat &&
                            selectedIssue.long === issue.long
                          ) {
                            deselectIssue(issue.service_request_id);
                            return;
                          } else {
                            clearIcons();
                            selectIssue(issue);
                          }
                        }}"
                      >
                        {issue.description ?? "-"}
                        {#if visibleDetails.has(issue.service_request_id)}
                          <!-- svelte-ignore a11y-click-events-have-key-events -->
                          <img
                            src="{detailSVG}"
                            style="background-color: {accentTwo}; vertical-align: middle"
                            alt="detail view"
                            height="17rem"
                            on:click|stopPropagation="{(e) => {
                              showModal = true;
                            }}"
                          />
                        {/if}
                      </td>

                      <td id="td-media" style="text-align: center">
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
                        id="td-reported-time"
                        style="text-align: center"
                        on:click="{() => {
                          if (
                            selectedIssue &&
                            selectedIssue.lat === issue.lat &&
                            selectedIssue.long === issue.long
                          ) {
                            deselectIssue(issue.service_request_id);
                            return;
                          } else {
                            clearIcons();
                            selectIssue(issue);
                          }
                        }}"
                      >
                        {formatRelativeDate(issue.requested_datetime)}
                      </td>
                    </tr>
                  {:else}
                    <tr>
                      <td
                        style="padding-left: {window.innerWidth >= 815
                          ? '0.5rem'
                          : '0'}">{messages["find.issue"]["empty.results"]}</td
                      >
                    </tr>
                  {/each}
                {:else}
                  <tr>
                    <td
                      style="padding-left: {window.innerWidth >= 815
                        ? '0.5rem'
                        : '0'}">{messages["find.issue"]["empty.results"]}</td
                    >
                  </tr>
                {/if}
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
