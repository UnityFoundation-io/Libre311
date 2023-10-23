<!-- Copyright 2023 Libre311 Authors-->

<!-- Licensed under the Apache License, Version 2.0 (the "License"); -->
<!-- you may not use this file except in compliance with the License. -->
<!-- You may obtain a copy of the License at -->

<!--     http://www.apache.org/licenses/LICENSE-2.0 -->

<!-- Unless required by applicable law or agreed to in writing, software -->
<!-- distributed under the License is distributed on an "AS IS" BASIS, -->
<!-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. -->
<!-- See the License for the specific language governing permissions and -->
<!-- limitations under the License. -->

<script>
  import { fade, fly } from "svelte/transition";
  import { createEventDispatcher } from "svelte";

  export let title;
  export let color;

  const dispatch = createEventDispatcher();

  function closeModal() {
    dispatch("cancel");
  }
</script>

<!-- svelte-ignore a11y-click-events-have-key-events -->
<div class="modal-backdrop" on:click="{closeModal}" transition:fade></div>
<div
  class="modal"
  transition:fly="{{ y: 300 }}"
  style="width: {window.innerWidth < 320
    ? '100vw'
    : '70vw'}; left: {window.innerWidth < 320 ? '0' : '15vw'}"
>
  <h1 style="color: {color}">{title}</h1>
  <div class="content">
    <slot />
  </div>
  <footer>
    <slot name="footer">
      <button
        class="button"
        style="float: right; margin-bottom: 1rem; background-image: radial-gradient(
          circle at 37%,
          rgba(255, 255, 255, 1) 51%,
          rgba(190, 212, 250, 0.9) 65%,
          rgba(190, 212, 250, 0.9) 100%
        );"
        on:click="{closeModal}">Close</button
      >
    </slot>
  </footer>
</div>

<style>
  .modal-backdrop {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: rgba(0, 0, 0, 0.75);
    z-index: 10;
  }

  .modal {
    position: fixed;
    top: 10vh;
    max-height: 80vh;
    background: white;
    border-radius: 15px;
    z-index: 100;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.26);
    overflow: scroll;
  }

  h1 {
    padding: 1rem;
    margin: 0;
    border-bottom: 1px solid #ccc;
    font-size: 1.3rem;
    text-align: center;
  }

  .content {
    padding: 1rem;
  }

  footer {
    padding: 1rem;
  }
</style>
