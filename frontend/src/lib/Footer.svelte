<script>
  import facebookSVG from "../icons/facebook.svg";
  import instagramSVG from "../icons/instagram.svg";
  import twitterSVG from "../icons/twitter.svg";
  import localMotionLogo from "$lib/localmotionlogosmall.png";
  import footerSelector from "../stores/footerSelector";

  export let backgroundColor;
</script>

<div
  class="footer"
  bind:this="{$footerSelector}"
  style="background-color: {backgroundColor}"
>
  <img
    src="{localMotionLogo}"
    alt="local motion logo"
    class="local-motion-logo"
  />

  <div
    class="row"
    class:row-wrap="{window.innerHeight <= 375 && window.innerWidth <= 513}"
  >
    <div
      class="col"
      class:tighten-portrait="{window.innerHeight <= 926 &&
        window.innerWidth <= 430}"
    >
      <a href="https://lomocomo.org/we-move-app/">
        <h2>Mobile App</h2>
      </a>
      <a href="https://lomocomo.org/about/contact-us/">
        <h2>Contact Us</h2>
      </a>
    </div>
    <div
      class="col"
      class:tighten-landscape="{window.innerWidth <= 677 &&
        window.innerHeight <= 375}"
    >
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
    <div class="col">
      <a href="https://lomocomo.org/get-involved/"><h2>Get Involved</h2></a>
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
  .footer {
    display: flex;
    flex-wrap: wrap;
    height: auto;
    justify-content: space-evenly;
    margin-top: 17.5rem;
  }

  .col {
    display: flex;
    flex-direction: column;
    padding-right: 2rem;
    padding-left: 2rem;
  }

  .tighten-portrait {
    margin-bottom: -3rem;
  }

  .tighten-landscape {
    margin-left: 4rem;
  }

  .row {
    display: flex;
    flex-direction: row;
    gap: 3rem;
  }

  .row-wrap {
    flex-wrap: wrap;
  }

  .socials {
    display: flex;
    flex-direction: row;
    padding-top: 1rem;
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

  .local-motion-logo {
    position: relative;
    top: -6.4rem;
    width: 150px;
    height: 104px;
  }

  @media only screen and (min-width: 320px) and (max-width: 375px) and (orientation: portrait) {
    .local-motion-logo {
      margin-bottom: -5rem;
      margin-left: 13rem;
      height: 69px;
      width: 100px;
      top: -4.2rem;
    }

    .row {
      flex-wrap: wrap;
    }
  }

  @media only screen and (min-width: 375px) and (max-width: 389px) and (orientation: portrait) {
    .local-motion-logo {
      margin-bottom: -5rem;
      margin-left: 15rem;
      height: 69px;
      width: 100px;
      top: -4.2rem;
    }

    .row {
      flex-wrap: wrap;
    }
  }

  @media only screen and (min-width: 390px) and (max-width: 430px) and (orientation: portrait) {
    .local-motion-logo {
      margin-left: 16rem;
      margin-bottom: -5rem;
      height: 69px;
      width: 100px;
      top: -4.2rem;
    }

    .row {
      flex-wrap: wrap;
    }
  }

  @media only screen and (min-width: 513px) and (max-width: 749px) and (max-height: 340px) and (orientation: landscape) {
    .local-motion-logo {
      margin-bottom: -5rem;
      height: 69px;
      width: 100px;
      margin-left: 33rem;
      top: -4.2rem;
    }
  }

  @media only screen and (min-width: 750px) and (max-width: 813px) and (max-height: 340px) and (orientation: landscape) {
    .local-motion-logo {
      margin-bottom: -5rem;
      height: 69px;
      width: 100px;
      top: -4.2rem;
      margin-left: 2.5rem;
    }

    .col {
      padding-left: 0;
      padding-right: 0;
      margin: 0 0.3rem 0 0.3rem;
    }
  }

  @media only screen and (min-width: 750px) and (max-width: 814px) and (max-height: 450px) and (orientation: landscape) {
    .local-motion-logo {
      margin-bottom: -5rem;
      height: 69px;
      width: 100px;
      left: -21.4rem;
      top: -4.2rem;
    }
  }
</style>
