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
  import { onMount, onDestroy, createEventDispatcher } from "svelte";
  import "flatpickr/dist/flatpickr.min.css";
  import flatpickr from "flatpickr";
  import issueTime from "../stores/issueTime"
  import resetDate from "../stores/resetDate";
  import messages from "$media/messages.json";
  import expandSVG from "../icons/expand.svg";

  const dispatch = createEventDispatcher();

  let picker;
  // let dateTimeInput;
  let ref;
  let value;

  $: if ($resetDate) {
    ref.value = "";
    picker.clear();
    picker = flatpickr(ref, {
      mode: "single",
      dateFormat: "Y-m-d\\TH:i:S\\Z",
      altInput: true,
      altFormat: "n/d/Y, h:i K",
      defaultDate: "today",
      enableTime: true,
      maxDate: "today",
      onClose: (selectedDates, dateStr, instance) => {
        dispatch("dateSelected", dateStr);
      },
    });

    resetDate.set(false);
  }

  onMount(() => {
    picker = flatpickr(ref, {
      mode: "single",
      dateFormat: "Y-m-d\\TH:i:S\\Z",
      altInput: true,
      altFormat: "n/d/Y, h:i K",
      defaultDate: "today",
      enableTime: true,
      maxDate: "today",
      onClose: (selectedDates, dateStr, instance) => {
        dispatch("dateSelected", dateStr);
      },
    });

    const clickEvent = new MouseEvent("click", {
      bubbles: true,
      cancelable: true,
      view: window,
    });

    caret.addEventListener("click", () => {
      ref.dispatchEvent(clickEvent);
    });
  });

  onDestroy(() => {
    picker.destroy();
  });
</script>

<div class="input-wrapper">
  <input
    type="text"
    bind:this="{ref}"
    placeholder="{messages['report.issue']['label.datetime']}"
    readonly
  />
  <img src="{expandSVG}" id="caret" alt="pick date range" width="18rem" />
</div>

<style>
  input {
    border-radius: 10px;
    height: 1.4rem;
    width: 16rem;
    border: 0.01rem solid black;
    border-style: inset;
    padding-left: 0.5rem;
    font-size: 1.1rem;
    color: #3478f6;
  }

  input::placeholder {
    color: #3478f6;
    font-size: 1.1rem;
  }

  .input-wrapper {
    position: relative;
    display: inline-block;
  }

  .input-wrapper input {
    padding-right: 20px;
  }

  #caret {
    position: absolute;
    right: 5px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
  }

  @media only screen and (min-width: 245px) and (max-width: 374px) {
    input::placeholder {
      font-size: 0.5rem;
    }

    input {
      height: 1.43rem;
      width: fit-content;
      font-size: 0.5rem;
    }
  }

  @media only screen and (min-width: 375px) and (max-width: 844px) {
    input::placeholder {
      font-size: 0.8rem;
    }

    input {
      height: 1.33rem;
      width: fit-content;
      font-size: 0.7rem;
      vertical-align: 0.05rem;
    }
  }

  @media only screen and (min-width: 390px) and (max-height: 669px) and (orientation: portrait) {
    input::placeholder {
      font-size: 0.7rem;
    }

    input {
      height: 1.4rem;
      width: fit-content;
      font-size: 0.7rem;
      vertical-align: -0.03rem;
    }
  }
</style>
