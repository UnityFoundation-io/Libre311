<script lang="ts">
	import Login from '$lib/components/Login/Login.svelte';
	import type { EventDispatchTypeMap } from '$lib/components/Login/shared';
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
	let loading = false;

	function handleChange(e: CustomEvent<EventDispatchTypeMap['inputChange']>) {
		if (e.detail.type == 'email') {
			emailInput.value = e.detail.value;
			emailInput = emailInput;
		} else {
			passwordInput.value = e.detail.value;
			passwordInput = passwordInput;
		}
	}

	function cancel() {
		history.back();
	}

	async function login() {
		emailInput = emailValidator(emailInput);
		passwordInput = passwordValidator(passwordInput);

		if (emailInput.type === 'valid' && passwordInput.type === 'valid') {
			loading = true;
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
			} finally {
				loading = false;
			}
		}
	}
</script>

<Login
	{emailInput}
	{passwordInput}
	bind:errorMessage
	{loading}
	on:inputChange={handleChange}
	on:login={login}
	on:cancel={cancel}
/>
