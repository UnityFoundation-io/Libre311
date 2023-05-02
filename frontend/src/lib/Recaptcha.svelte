<script>
  import { onMount } from "svelte";

  export let sitekey;
  let recaptchaElement;

  onMount(() => {
    window.grecaptcha.render(recaptchaElement, { sitekey });
  });
</script>

<div
  bind:this="{recaptchaElement}"
  class="g-recaptcha"
  data-sitekey="{sitekey}"
></div>
