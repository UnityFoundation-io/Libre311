import { writable } from 'svelte/store';

const userCurrentLocation = writable(null);

export default userCurrentLocation;