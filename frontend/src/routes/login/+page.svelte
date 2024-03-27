<script lang="ts">
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import LoginDesktop from '$lib/components/Login/LoginDesktop.svelte';
	import type { EventDispatchTypeMap } from '$lib/components/Login/shared';
	import LoginMobile from '$lib/components/Login/LoginMobile.svelte';
	import { createInput, emailValidator } from '$lib/utils/validation';
	import { useLibre311Context, useUnityAuthService } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';

	const authService = useUnityAuthService();
	const alertError = useLibre311Context().alertError;
	const alert = useLibre311Context().alert;

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

	async function login() {
		emailInput = emailValidator(emailInput);

		if (emailInput.value && passwordInput.value) {
			try {

				const res = await authService.login(emailInput.value, passwordInput.value);

				alert({
					type: 'success',
					title: 'Success',
					description: 'You are logged in'
				});

				goto('/issues/table');
			} catch (error) {
				alertError(error);
			}
		}
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
