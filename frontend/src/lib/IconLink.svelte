<script>
  export let href;
  export let target = "_self";
</script>

<a {href} {target}><slot /></a>

<style>
  a:hover {
    background-color: rgba(0, 0, 0, 0.04);
  }
  a {
    display: inline-flex;
    padding: 8px;
    border-radius: 50%;
    overflow: visible;
    transition: background-color 150ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;
  }
</style>
