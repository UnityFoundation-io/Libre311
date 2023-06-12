<script>
  import { goto } from "$app/navigation";
  import facebookSVG from "../icons/facebook.svg";
  import instagramSVG from "../icons/instagram.svg";
  import twitterSVG from "../icons/twitter.svg";
  import localMotionLogo from "$lib/localmotionlogosmall.webp";
  import footerSelector from "../stores/footerSelector";

  export let backgroundColor;

  const URL_PREFIX = import.meta.env.VITE_BACKEND_URL;
</script>

<img
  src="{localMotionLogo}"
  alt="local motion logo"
  class="local-motion-logo"
/>
<div
  class="footer"
  bind:this="{$footerSelector}"
  style="background-color: {backgroundColor}"
>
  <div
    class="row"
    class:row-wrap="{window.innerHeight <= 375 && window.innerWidth <= 513}"
  >
    <div class="col">
      <a href="https://lomocomo.org/?ct_template=advocacy"><h2>Advocacy</h2></a>
      <a href="https://lomocomo.org/advocacy/livable-streets/"
        ><li>Livable Streets</li></a
      >
      <a href="https://lomocomo.org/advocacy/transportation-planning/"
        ><li>Transportation Planning</li></a
      >
      <a href="https://lomocomo.org/advocacy/grassroots-organizing/"
        ><li>Grassroots Organizing</li></a
      >
      <a
        href="https://lomocomo.org/advocacy/missourians-for-responsible-transportation/"
        ><li>State-Level Advocacy</li></a
      >
    </div>
    <div
      class="col"
      class:add-padding-top="{window.innerWidth >= 431 &&
        window.innerWidth < 700}"
    >
      <a href="https://lomocomo.org/get-involved/"
        ><h2 style="min-width: max-content">Get Involved</h2></a
      >
      <a href="https://lomocomo.org/get-involved/membership/"
        ><li>Membership</li></a
      >
      <a href="https://lomocomo.org/get-involved/volunteer/"
        ><li>Volunteer</li></a
      >
      <a href="https://lomocomo.org/get-involved/sign-up-for-our-newsletter/"
        ><li>Sign up for our newsletter</li></a
      >
    </div>

    <div class="col">
      <a href="https://lomocomo.org/about/"><h2>About</h2></a>
      <a href="https://lomocomo.org/about/staff/"><li>Staff</li></a>
      <a href="https://lomocomo.org/about/board-of-directors/"
        ><li>Board of Directors</li></a
      >
      <a href="https://lomocomo.org/about/annual-reports"
        ><li>Annual Reports</li></a
      >
      <a href="https://lomocomo.org/about/legal-and-financial/"
        ><li>Legal and Financial</li></a
      >
      <a href="https://lomocomo.org/about/the-rebrand-story/"
        ><li>Our Rebrand Story</li></a
      >
    </div>
    <div class="col">
      <!-- svelte-ignore missing-declaration -->
      <!-- svelte-ignore a11y-click-events-have-key-events -->
      <h2
        style="cursor: pointer; min-width: max-content"
        on:click="{() => goto(`${URL_PREFIX}/oauth/login/google`, true)}"
      >
        Admin Login
      </h2>
      <a href="https://lomocomo.org/about/contact-us/">
        <h2>Contact Us</h2>
      </a>

      <div class="socials">
        <a class="socialButton" href="https://facebook.com/localmotioncomo">
          <img src="{facebookSVG}" alt="facebook" style="scale:4%;" />
        </a>
        <a
          class="socialButton"
          href="https://www.instagram.com/localmotioncomo/"
        >
          <img src="{instagramSVG}" alt="instagram" style="scale:2.5%;" />
        </a>
        <a class="socialButton" href="https://twitter.com/localmotioncomo">
          <img src="{twitterSVG}" alt="twitter" style="scale:2.5%;" />
        </a>
      </div>
    </div>
  </div>
</div>

<style>
  h2 {
    font-size: 1.2rem;
  }

  li {
    font-size: 0.85rem;
  }

  .local-motion-logo {
    margin-top: 5rem;
    margin-bottom: -0.4rem;
    height: 69px;
    width: 100px;
  }

  .footer {
    display: flex;
    flex-wrap: wrap;
    height: auto;
    justify-content: space-evenly;
  }

  .col {
    display: flex;
    flex-direction: column;
    padding-right: 2rem;
    padding-left: 2rem;
    min-width: 10rem;
    max-width: 10rem;
  }

  .row {
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .row-wrap {
    flex-wrap: wrap;
  }

  .socials {
    display: flex;
    flex-direction: row;
  }

  .socialButton {
    align-items: center;
    background-color: #f2a900;
    border-radius: 50%;
    display: flex;
    font-size: 1.5em;
    height: 2rem;
    justify-content: center;
    margin-bottom: 0.5rem;
    margin-right: 0.5rem;
    width: 2rem;
  }

  .footer a {
    text-decoration: none;
  }

  .footer h2 {
    color: #f2a900;
  }

  .footer li {
    color: white;
    list-style-type: none;
  }

  .footer li:hover {
    color: #f2a900;
  }

  @media only screen and (min-width: 245px) {
    .row {
      flex-wrap: wrap;
    }
  }

  @media only screen and (min-width: 750px) and (max-width: 813px) and (max-height: 340px) and (orientation: landscape) {
    .col {
      padding-left: 0;
      padding-right: 0;
      margin: 0 0.3rem 0 0.3rem;
    }
  }

  @media only screen and (min-width: 700px) {
    .row {
      flex-wrap: nowrap;
      gap: 0;
    }

    .col {
      padding-left: 0.4rem;
      padding-right: 0.4rem;
    }
  }

  @media only screen and (min-width: 770px) {
    .col {
      min-width: 10.3rem;
    }
  }

  @media only screen and (min-width: 245px) and (max-width: 415px) {
    .row {
      justify-content: left;
    }
  }

  @media only screen and (min-width: 415px) and (max-width: 447px) {
    .col {
      min-width: 9rem;
      max-width: 9rem;
    }
  }

  @media only screen and (min-width: 624px) and (max-width: 699px) {
    .col {
      min-width: 11rem;
    }
  }

  @media only screen and (min-width: 815px) {
    .col {
      min-width: 12rem;
      max-width: 12rem;
    }
  }

  @media only screen and (min-width: 930px) {
    .col {
      min-width: 13.5rem;
      max-width: 13.5rem;
    }
  }
</style>
