<script>
  export let variant = "text";
</script>

<button class="{variant}-variant" on:click><slot /> </button>

<style>
  /* css reset for button */
  .text-variant {
    background: none repeat scroll 0 0 transparent;
    border: medium none;
    border-spacing: 0;
    color: #26589f;
    font-family: "PT Sans Narrow", sans-serif;
    font-size: 16px;
    font-weight: normal;
    line-height: 1.42rem;
    list-style: none outside none;
    margin: 0;
    padding: 0;
    text-align: left;
    text-decoration: none;
    text-indent: 0;
  }
</style>
