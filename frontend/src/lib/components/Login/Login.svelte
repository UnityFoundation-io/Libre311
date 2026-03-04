<script lang="ts">
	import messages from '$media/messages.json';
	import { rocketLaunch } from '$lib/components/Svg/outline/rocket-launch.js';
	import { Card } from 'stwui';
	import { fade, draw } from 'svelte/transition';
	import { onMount } from 'svelte';
	import { type FormInputValue } from '$lib/utils/validation';
	import LoginForm from './LoginForm.svelte';

	export let emailInput: FormInputValue<string | undefined>;
	export let passwordInput: FormInputValue<string | undefined>;
	export let errorMessage: string | undefined;
	export let loading = false;

	let visible = false;
	let loginForm: LoginForm;

	onMount(() => {
		visible = true;
		loginForm.focus();
	});
</script>

<div class="min-h-full w-full sm:flex sm:items-center sm:justify-center sm:bg-primary">
	<Card class="w-full border-none shadow-none sm:w-1/3 sm:max-w-md sm:border-solid sm:shadow-md">
		<div class="m-4 flex flex-col items-center justify-center">
			{#if visible}
				<div class="size-28">
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="1"
						stroke="hsl(var(--primary))"
						xml:space="preserve"
					>
						<g out:fade={{ duration: 200 }} opacity="1">
							<path
								class="st0"
								in:draw={{ duration: 3000 }}
								stroke-linecap="round"
								stroke-linejoin="round"
								style="stroke-width: 1"
								d={rocketLaunch}
							/>
						</g>
					</svg>
				</div>
			{/if}

			<h1 class="text-lg">{messages['login']['title']}</h1>
		</div>

		<div class="m-4">
			<LoginForm
				bind:this={loginForm}
				{emailInput}
				{passwordInput}
				{errorMessage}
				{loading}
				on:inputChange
				on:login
				on:cancel
			/>
		</div>

		<div class="m-4">
			<!-- eslint-disable-next-line svelte/valid-compile -->
			<a class="inline-block min-h-[24px] text-sm" href="javascript:void(0);"
				>{messages['login']['forgot_password']}</a
			>
		</div>

		<div class="m-4">
			<!-- eslint-disable-next-line svelte/valid-compile -->
			<a class="inline-block min-h-[24px] text-sm" href="javascript:void(0);"
				>{messages['login']['create_account']}</a
			>
		</div>
	</Card>
</div>

<style>
	:global(.sm\:bg-primary) {
		background-color: hsl(var(--primary));
	}

	a {
		text-decoration: underline;
		color: hsl(var(--secondary-content));
	}

	a:hover {
		color: hsl(var(--primary));
	}
</style>
