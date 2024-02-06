<script lang="ts">
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import LoginDesktop from '$lib/components/Login/LoginDesktop.svelte';
	import type { EventDispatchTypeMap } from '$lib/components/Login/shared';
	import LoginMobile from '$lib/components/Login/LoginMobile.svelte';
	import { createInput, emailValidator } from '$lib/utils/validation';

	let emailInput = createInput('');
	let passwordInput = createInput('');

	function handleChange(e: CustomEvent<EventDispatchTypeMap['inputChange']>) {
		if (e.detail.type == 'email') {
			emailInput.value = e.detail.value;
			emailInput = emailInput;
		} else {
			passwordInput.value = e.detail.value;
			passwordInput = passwordInput;
		}
	}

	function login() {
		emailInput = emailValidator(emailInput);
	}
</script>

<Breakpoint>
	<LoginDesktop
		slot="is-desktop"
		{emailInput}
		{passwordInput}
		on:inputChange={handleChange}
		on:login={login}
	/>
	<LoginMobile
		slot="is-mobile-or-tablet"
		{emailInput}
		{passwordInput}
		on:inputChange={handleChange}
		on:login={login}
	/>
</Breakpoint>
