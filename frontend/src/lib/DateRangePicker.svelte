<script>
  import { onMount, onDestroy, createEventDispatcher } from "svelte";
  import "flatpickr/dist/flatpickr.min.css";
  import flatpickr from "flatpickr";
  import resetDate from "../stores/resetDate";
  import messages from "$lib/messages.json";
  import expandSVG from "../icons/expand.svg";

  const dispatch = createEventDispatcher();

  let picker;
  let dateRangeInput;

  $: if ($resetDate) {
    dateRangeInput.value = "";
    picker.clear();
    picker = flatpickr(dateRangeInput, {
      mode: "range",
      dateFormat: "Y-m-d",
      onClose: (selectedDates, dateStr, instance) => {
        let formattedDates = [];

        if (selectedDates.length == 2) {
          let startDate = instance.formatDate(
            selectedDates[0],
            "Y-m-d\\TH:i:ss\\Z"
          );
          formattedDates.push(startDate);

          let endDate = selectedDates[1];
          endDate.setHours(23, 59, 59);
          endDate = instance.formatDate(selectedDates[1], "Y-m-d\\TH:i:s\\Z");
          formattedDates.push(endDate);
        }

        dispatch("datesSelected", formattedDates);
      },
    });

    resetDate.set(false);
  }

  onMount(() => {
    picker = flatpickr(dateRangeInput, {
      mode: "range",
      dateFormat: "Y-m-d",
      onClose: (selectedDates, dateStr, instance) => {
        let formattedDates = [];

        if (selectedDates.length == 2) {
          let startDate = instance.formatDate(
            selectedDates[0],
            "Y-m-d\\TH:i:ss\\Z"
          );
          formattedDates.push(startDate);

          let endDate = selectedDates[1];
          endDate.setHours(23, 59, 59);
          endDate = instance.formatDate(selectedDates[1], "Y-m-d\\TH:i:s\\Z");
          formattedDates.push(endDate);
        }

        dispatch("datesSelected", formattedDates);
      },
    });

    const clickEvent = new MouseEvent("click", {
      bubbles: true,
      cancelable: true,
      view: window,
    });

    caret.addEventListener("click", () => {
      dateRangeInput.dispatchEvent(clickEvent);
    });
  });

  onDestroy(() => {
    picker.destroy();
  });
</script>

<div class="input-wrapper">
  <input
    type="text"
    bind:this="{dateRangeInput}"
    placeholder="{messages['find.issue']['select.date.range.placeholder']}"
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

  @media only screen and (min-width: 320px) and (max-width: 374px) {
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
