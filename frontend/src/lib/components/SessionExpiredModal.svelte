<script lang="ts">
	import { Portal } from 'stwui';
	import messages from '$media/messages.json';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { tick } from 'svelte';
	import { isAxiosError } from 'axios';
	import LoginForm from './Login/LoginForm.svelte';
	import { createInput, emailValidator, passwordValidator } from '$lib/utils/validation';

	const { unityAuthService, sessionExpired, user } = useLibre311Context();

	let emailInput = createInput('');
	let passwordInput = createInput('');
	let errorMessage: string | undefined;
	let loading = false;
	let loginForm: LoginForm;

	async function handleLogin() {
		emailInput = emailValidator(emailInput);
		passwordInput = passwordValidator(passwordInput);

		if (emailInput.type === 'valid' && passwordInput.type === 'valid') {
			loading = true;
			errorMessage = undefined;
			try {
				const previousUsername = $user?.username;
				const newLoginData = await unityAuthService.login(emailInput.value, passwordInput.value);

				if (previousUsername && newLoginData.username !== previousUsername) {
					window.location.reload();
					return;
				}

				$sessionExpired = false;
				emailInput = createInput('');
				passwordInput = createInput('');
			} catch (e: unknown) {
				console.error(e);
				if (isAxiosError(e)) {
					errorMessage = e.response?.data?.message || e.message;
				} else if (e instanceof Error) {
					errorMessage = e.message;
				} else {
					errorMessage = 'Login failed';
				}
			} finally {
				loading = false;
			}
		}
	}

	function handleInputChange(
		e: CustomEvent<{ type: 'email' | 'password'; value: string | undefined }>
	) {
		if (e.detail.type === 'email') {
			emailInput.value = e.detail.value;
			emailInput = emailInput;
		} else {
			passwordInput.value = e.detail.value;
			passwordInput = passwordInput;
		}
	}

	$: if ($sessionExpired) {
		tick().then(() => {
			loginForm.focus();
		});
	}
</script>

{#if $sessionExpired}
	<Portal>
		<div
			class="fixed inset-0 z-[9999] flex items-center justify-center bg-gray-500/30 backdrop-blur-md transition-all"
		>
			<div class="mx-4 w-full max-w-md rounded-2xl border border-gray-100 bg-white p-8 shadow-2xl">
				<div class="flex flex-col gap-6">
					<header class="text-center">
						<div
							class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-amber-100"
						>
							<svg
								xmlns="http://www.w3.org/2000/svg"
								class="h-8 w-8 text-amber-600"
								fill="none"
								viewBox="0 0 24 24"
								stroke="currentColor"
							>
								<path
									stroke-linecap="round"
									stroke-linejoin="round"
									stroke-width="2"
									d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
								/>
							</svg>
						</div>
						<h2 class="text-2xl font-bold text-gray-900">{messages['login']['title']}</h2>
						<p class="mt-2 text-sm text-gray-500">
							Your session has expired. Please re-authenticate to save your changes and continue.
						</p>
					</header>

					<LoginForm
						bind:this={loginForm}
						{emailInput}
						{passwordInput}
						{errorMessage}
						{loading}
						showCancel={false}
						on:inputChange={handleInputChange}
						on:login={handleLogin}
					/>
				</div>
			</div>
		</div>
	</Portal>
{/if}

<style>
	/* Background animation */
	header div {
		animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
	}

	@keyframes pulse {
		0%,
		100% {
			opacity: 1;
		}
		50% {
			opacity: 0.7;
		}
	}
</style>
