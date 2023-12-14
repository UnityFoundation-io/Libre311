<script>
  export let ariaLabel;
</script>

<button aria-label={ariaLabel} class="button-reset" on:click><slot /></button>

<style>
  button:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }

  button {
    display: inline-flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    position: relative;
    box-sizing: border-box;
    -webkit-tap-highlight-color: transparent;
    background-color: transparent;
    outline: 0px;
    border: 0px;
    margin: 0px;
    cursor: pointer;
    user-select: none;
    vertical-align: middle;
    appearance: none;
    text-decoration: none;
    text-align: center;
    flex: 0 0 auto;
    font-size: 1.5rem;
    padding: 8px;
    border-radius: 50%;
    overflow: visible;
    color: rgb(255, 255, 255);
    transition: background-color 150ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;
  }
</style>
