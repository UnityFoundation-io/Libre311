<script>
  import { goto } from "$app/navigation";


  import facebookSVG from "../icons/facebook.svg";
  import instagramSVG from "../icons/instagram.svg";
  import twitterSVG from "../icons/twitter.svg";

  import footerSelector from "../stores/footerSelector";
  import footerLogoSmall from "$media/footerLogoSmall.webp";
  import footerLinks from "$media/footerLinks.json"
  import socialMediaLinks from "$media/socialMediaLinks.json";


  export let backgroundColor;

  const URL_PREFIX = import.meta.env.VITE_BACKEND_URL;


</script>

<img
  src="{footerLogoSmall}"
  alt="footer logo"
  class="footer-logo"
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
    {#each footerLinks as footerLink}
      <div class="col">
        <a href={footerLink["headerUrl"]}><h2>{footerLink["header"]}</h2></a>
        {#each footerLink["links"] as link}
          <a href={link["url"]}><li>{link["text"]}</li></a>
        {/each}
      </div>
    {/each}
     
    <div class="col">
      <!-- svelte-ignore missing-declaration -->
      <!-- svelte-ignore a11y-click-events-have-key-events -->
      <h2
        style="cursor: pointer; min-width: max-content"
        on:click="{() => goto(`${URL_PREFIX}/oauth/login/google`, true)}"
      >
        Admin Login
      </h2>

      {#if socialMediaLinks["contactUs"]}
        <a href={socialMediaLinks["contactUs"]}>
          <h2>Contact Us</h2>
        </a>
      {/if}

      <div class="socials">
        
        {#if socialMediaLinks["facebook"]}
          <a class="socialButton" href={socialMediaLinks["facebook"]}>
            <img src="{facebookSVG}" alt="facebook" />
          </a>
        {/if}

        {#if socialMediaLinks["instagram"]}
          <a
            class="socialButton"
            href={socialMediaLinks["instagram"]}
          >
            <img src="{instagramSVG}" alt="instagram" />
          </a>
        {/if}

        {#if socialMediaLinks["twitter"]}
          <a class="socialButton" href={socialMediaLinks["twitter"]}>
            <img src="{twitterSVG}" alt="twitter" />
          </a>
        {/if}
      
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

  .footer-logo {
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
