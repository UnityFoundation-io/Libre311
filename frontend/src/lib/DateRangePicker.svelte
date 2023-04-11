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
        const formattedDates = selectedDates.map((date) =>
          instance.formatDate(date, "Y-m-d")
        );

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
        const formattedDates = selectedDates.map((date) =>
          instance.formatDate(date, "Y-m-d")
        );

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
    height: 2.2rem;
    width: 13rem;
    border: solid 1px black;
    padding-top: 0.15rem;
    padding-left: 0.5rem;
    font-size: 1.1rem;
  }

  input::placeholder {
    color: black;
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

  @media only screen and (min-width: 375px) and (max-width: 844px) {
    input::placeholder {
      font-size: 0.7rem;
    }

    input {
      width: fit-content;
      font-size: 0.7rem;
    }
  }
</style>
