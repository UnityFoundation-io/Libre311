<script>
  import { onMount, createEventDispatcher } from "svelte";

  let primaryFont = "Gotham";
  let primaryFontAvailable = false;

  const dispatch = createEventDispatcher();

  onMount(async () => {
    setTimeout(async () => {
      primaryFontAvailable = await checkPrimaryFontAvailability();
      if (!primaryFontAvailable) dispatch("primaryFontNotAvailable");
      else dispatch("primaryFontAvailable");
    }, 500);
  });

  async function checkPrimaryFontAvailability(testText = "WgQqy@#%|") {
    if ("fonts" in document) {
      await document.fonts.ready;
    }

    const primaryElement = document.createElement("span");
    const fallbackElement = document.createElement("span");
    const customFallbackFont = "Roboto";

    primaryElement.style.cssText = `font-size: 72px; position: absolute; left: -9999px; top: -9999px; font-family: ${primaryFont}, ${customFallbackFont}`;
    fallbackElement.style.cssText = `font-size: 72px; position: absolute; left: -9999px; top: -9999px; font-family: ${customFallbackFont}`;

    primaryElement.textContent = testText;
    fallbackElement.textContent = testText;

    document.body.appendChild(primaryElement);
    document.body.appendChild(fallbackElement);

    const primaryElementWidth = primaryElement.offsetWidth;
    const fallbackElementWidth = fallbackElement.offsetWidth;

    document.body.removeChild(primaryElement);
    document.body.removeChild(fallbackElement);

    return primaryElementWidth !== fallbackElementWidth;
  }
</script>
