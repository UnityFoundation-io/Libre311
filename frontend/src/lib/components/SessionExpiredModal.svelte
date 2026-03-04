<script lang="ts">
	import { Button, Input, Portal } from 'stwui';
	import messages from '$media/messages.json';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { tick } from 'svelte';
	import { isAxiosError } from 'axios';

	const { unityAuthService, sessionExpired, user } = useLibre311Context();

	let email = '';
	let password = '';
	let errorMessage: string | undefined;
	let loading = false;

	async function handleLogin() {
		loading = true;
		errorMessage = undefined;
		try {
			const previousUsername = $user?.username;
			const newLoginData = await unityAuthService.login(email, password);

			if (previousUsername && newLoginData.username !== previousUsername) {
				window.location.reload();
				return;
			}

			$sessionExpired = false;
			email = '';
			password = '';
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

	function handleKeydown(e: KeyboardEvent) {
		if (e.key === 'Enter') {
			handleLogin();
		}
	}

	$: if ($sessionExpired) {
		tick().then(() => {
			const emailInput = document.getElementById('session-expiry-email');
			emailInput?.querySelector('input')?.focus();
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

					{#if errorMessage}
						<div
							role="alert"
							class="flex items-center gap-3 rounded-xl border border-red-100 bg-red-50 p-4 text-sm text-red-700"
						>
							<svg
								xmlns="http://www.w3.org/2000/svg"
								class="h-5 w-5 shrink-0"
								viewBox="0 0 20 20"
								fill="currentColor"
							>
								<path
									fill-rule="evenodd"
									d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
									clip-rule="evenodd"
								/>
							</svg>
							{errorMessage}
						</div>
					{/if}

					<div class="flex flex-col gap-5">
						<Input
							id="session-expiry-email"
							type="email"
							name="username"
							autocomplete="username"
							placeholder={messages['login']['email']['placeholder']}
							bind:value={email}
							on:keydown={handleKeydown}
						>
							<Input.Label slot="label">{messages['login']['email']['label']}</Input.Label>
						</Input>

						<Input
							id="session-expiry-password"
							type="password"
							name="password"
							autocomplete="current-password"
							showPasswordToggle
							placeholder={messages['login']['password']['placeholder']}
							bind:value={password}
							on:keydown={handleKeydown}
						>
							<Input.Label slot="label">{messages['login']['password']['label']}</Input.Label>
						</Input>
					</div>

					<footer class="mt-2">
						<Button
							on:click={handleLogin}
							type="primary"
							{loading}
							class="h-12 w-full rounded-xl text-lg font-semibold transition-all active:scale-[0.98]"
						>
							{messages['login']['submit']}
						</Button>
					</footer>
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
