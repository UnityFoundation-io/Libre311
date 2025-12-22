<script lang="ts">
	import messages from '$media/messages.json';
	import { rocketLaunch } from '$lib/components/Svg/outline/rocket-launch.js';
	import { Button, Card, Input } from 'stwui';
	import { fade, draw } from 'svelte/transition';
	import { createEventDispatcher, onMount } from 'svelte';
	import { type FormInputValue } from '$lib/utils/validation';
	import { dispatchEventFunctionFactory, type EventDispatchTypeMap } from './shared';

	const dispatch = createEventDispatcher<EventDispatchTypeMap>();

	export let emailInput: FormInputValue<string | undefined>;
	export let passwordInput: FormInputValue<string | undefined>;
	export let errorMessage: string | undefined;

	let visible = false;

	onMount(() => {
		visible = true;
	});

	const { onChange, onSubmit } = dispatchEventFunctionFactory(dispatch);

	// pass svelte checks
	const passwordAutocomplete = "current-password" as any;
	const emailAutocomplete = "email" as any;
</script>

<div
	class="flex h-full w-full items-center justify-center"
	style="background-color: hsl(var(--primary));"
>
	<Card class="sm:w-1/3">
		{#if errorMessage}
			<div class="flex justify-center rounded-t-md bg-red-500 p-2 text-white">
				<span>{errorMessage}</span>
			</div>
		{/if}
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
			<Input
				allowClear
				id="email-desktop"
				type="email"
				name="email-desktop"
				placeholder={messages['login']['email']['placeholder']}
				error={emailInput.error}
				value={emailInput.value}
				autocomplete={emailAutocomplete}
				on:change={(e) => onChange(e, 'email')}
			>
				<Input.Label slot="label">{messages['login']['email']['label']}</Input.Label>
			</Input>
		</div>

		<div class="m-4">
			<Input
				allowClear
				id="password-desktop"
				type="password"
				name="password-desktop"
				autocomplete={passwordAutocomplete}
				showPasswordToggle={true}
				placeholder={messages['login']['password']['placeholder']}
				error={passwordInput.error}
				value={passwordInput.value}
				on:change={(e) => onChange(e, 'password')}
			>
				<Input.Label slot="label">{messages['login']['password']['label']}</Input.Label>
			</Input>
		</div>

		<div class="m-4">
			<Button type="primary" on:click={onSubmit}>
				{messages['login']['submit']}
			</Button>
		</div>

		<div class="m-4">
			<!-- disbling the warning until we have valid link for this -->
			<!-- eslint-disable-next-line svelte/valid-compile -->
			<a class="text-sm" href="javascript:void(0);">{messages['login']['forgot_password']}</a>
		</div>

		<div class="m-4">
			<!-- disbling the warning until we have valid link for this -->
			<!-- eslint-disable-next-line svelte/valid-compile -->
			<a class="text-sm" href="javascript:void(0);">{messages['login']['create_account']}</a>
		</div>
	</Card>
</div>

<style>
	a {
		text-decoration: underline;
		color: hsl(var(--secondary-content));
	}

	a:hover {
		color: hsl(var(--primary));
	}
</style>
