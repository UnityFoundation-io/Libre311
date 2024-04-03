import { browser } from '$app/environment';
import { writable } from 'svelte/store';

const loginToken = writable<string>(undefined);

loginToken.subscribe((value) => {
	if (browser) window.localStorage.setItem('loginToken', value);
});

export default loginToken;

// import { type Writable, writable } from 'svelte/store';

// export const loginToken: Writable<string | null> = writable(localStorage.getItem("loginToken") || null)
// loginToken.subscribe((val: string | null) => localStorage.setItem("loginToken", val));
