<script lang="ts">
	import Breakpoint from '$lib/components/Breakpoint.svelte';
	import LoginDesktop from '$lib/components/Login/LoginDesktop.svelte';
	import type { EventDispatchTypeMap } from '$lib/components/Login/shared';
	import LoginMobile from '$lib/components/Login/LoginMobile.svelte';
	import { createInput, emailValidator, passwordValidator } from '$lib/utils/validation';
	import { useUnityAuthService } from '$lib/context/Libre311Context';
	import { goto } from '$app/navigation';
	import {
		checkHasMessage,
		isHateoasErrorResponse
	} from '$lib/services/Libre311/types/ServerErrors';
	import { isAxiosError } from 'axios';

	const authService = useUnityAuthService();

	let emailInput = createInput('');
	let passwordInput = createInput('');
	let errorMessage: string | undefined;

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
		passwordInput = passwordValidator(passwordInput);

		if (emailInput.value && passwordInput.value !== undefined) {
			try {
				await authService.login(emailInput.value, passwordInput.value);

				goto('/issues/table');
			} catch (error: unknown) {
				if (isAxiosError(error) && isHateoasErrorResponse(error.response?.data)) {
					const hateoasError = error.response.data;
					errorMessage = hateoasError.message;
				} else if (checkHasMessage(error)) {
					errorMessage = error.message;
				} else {
					errorMessage = new String(error).toString();
				}
			}
		}
	}
</script>

<Breakpoint>
	<LoginDesktop
		slot="is-desktop"
		{emailInput}
		{passwordInput}
		{errorMessage}
		on:inputChange={handleChange}
		on:login={login}
	/>
	<LoginMobile
		slot="is-mobile-or-tablet"
		{emailInput}
		{passwordInput}
		{errorMessage}
		on:inputChange={handleChange}
		on:login={login}
	/>
</Breakpoint>
