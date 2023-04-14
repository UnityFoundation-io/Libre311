import { writable } from 'svelte/store';

const userCurrentLocation = writable({
    lat: "38.95180510457306",
    lng: "-92.32740864543621",
  });

export default userCurrentLocation;