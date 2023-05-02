<script>
  export let sitekey;
  let recaptchaElement;

  export function renderRecaptcha(callback) {
    if (typeof grecaptcha !== "undefined") {
      grecaptcha.enterprise.ready(async () => {
        const token = await grecaptcha.enterprise.execute(sitekey, {
          action: "submit",
        });

        if (typeof callback === "function") {
          callback(token);
        }
      });
    } else {
      setTimeout(renderRecaptcha, 100);
    }
  }

  window.onRecaptchaLoad = renderRecaptcha;
</script>

<div
  bind:this="{recaptchaElement}"
  class="g-recaptcha"
  data-sitekey="{sitekey}"
></div>
