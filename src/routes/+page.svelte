<script>
  import { Loader } from "@googlemaps/js-api-loader";
  import { fade, scale, blur } from "svelte/transition";
  import { quintOut } from "svelte/easing";
  import { onMount } from "svelte";
  import logo from "$lib/logo.png";
  import addSVG from "../icons/add.svg";
  import closeSVG from "../icons/close.svg";
  import searchSVG from "../icons/search.svg";
  import "$lib/global.css";

  const startRendering = 2000;
  const loader = new Loader({
    apiKey: "AIzaSyAGw4BZzLX7ssu7orvgqw3cvgE-Bu_UD1Y",
    version: "weekly",
    libraries: [],
  });

  let openLogo = false,
    fadeInBackground = false,
    openWeMove = false,
    reduceBackGroundOpacity = false,
    reportNewIssue = false;

  let backgroundSelector, sectionNewReport, map, input;

  let zoom = 13;

  function scrollToSection() {
    if (reportNewIssue) {
      window.scrollTo({ top: 0, behavior: "smooth" });
      return;
    }

    const y =
      sectionNewReport.getBoundingClientRect().top + window.pageYOffset + 200;
    window.scrollTo({ top: y, behavior: "smooth" });
  }

  onMount(() => {
    window.scrollTo({ top: 0, behavior: "smooth" });
    // Trigger the Svelte Transitions
    fadeInBackground = true;
    openLogo = true;
    openWeMove = true;

    loader.load().then(async (google) => {
      map = new google.maps.Map(document.getElementById("map"), {
        zoom: 8,
        center: { lat: 38.6740015313782, lng: -90.453269188364 },
      });
    });

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
    transition:fade="{{ duration: 3000, quintOut, amount: 10 }}"
  >
    <div style="display: flex; align-items: center; z-index: 1">
      {#if openLogo}
        <img
          transition:scale="{{
            delay: startRendering,
            duration: 1000,
            quintOut,
          }}"
          src="{logo}"
          alt="we move"
          height="170rem"
          style="filter: drop-shadow(3px 3px 3px black); margin-left: 2.5rem; margin-top: 2rem"
        />
      {/if}
      {#if openWeMove}
        <div
          class="we-move"
          transition:blur="{{
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
      transition:fade="{{ delay: startRendering, duration: 1000, quintOut }}"
    >
      <div class="slogan-title">Empowering Cities Together:</div>
      <div class="slogan-text">
        &nbsp;&nbsp; Notice, Report, and Enhance City Life with Our All-in-One
        Mobile App!
      </div>

      <div
        class="action-buttons"
        style="display: flex; justify-content: space-around"
      >
        {#if !reportNewIssue}
          <button
            class="button"
            style="background-image: radial-gradient(
      circle at 4% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    );"
          >
            <img
              src="{searchSVG}"
              alt="search for reported issues"
              style="vertical-align: -0.25rem; margin-right: 1.6rem; margin-left: -0.7rem"
              height="23rem"
            />
            Find a Reported Issue
          </button>
        {/if}

        <button
          class="button"
          style="right: 0; background-image: radial-gradient(
      circle at 6% 60%,
      rgba(190, 212, 250, 0.9),
      rgba(190, 212, 250, 0.9) 14%,
      white 20%,
      white 100%
    );"
          on:click="{() => {
            scrollToSection();
            reportNewIssue = !reportNewIssue;
          }}"
        >
          {#if !reportNewIssue}
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
      </div>
    </div>

    <div class:hidden="{!reportNewIssue}">
      <center>
        <div class="describe-issue">1. Where is the issue located?</div>
        <div bind:this="{sectionNewReport}" id="map"></div>
      </center>
    </div>
  </div>
{/if}

<style>
  .background {
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    position: relative;
    height: 1500px; /* Adjust this value according to your needs */
    background-repeat: no-repeat;
  }

  .background::before {
    content: "";
    background-image: url("$lib/streetview16-9.png");
    background-repeat: no-repeat center center fixed;
    background-position: 100% 42.5%;
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
    transition: all 5s;
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

  .hidden {
    visibility: hidden;
  }

  #map {
    width: 55vw;
    height: 55vh;
    margin-top: 3rem;
    align-items: center;
  }

  .describe-issue {
    margin-top: 3rem;
    font-size: 2rem;
    color: white;
    text-shadow: 2px 3px 2px rgba(0, 0, 0, 0.8);
  }
</style>
