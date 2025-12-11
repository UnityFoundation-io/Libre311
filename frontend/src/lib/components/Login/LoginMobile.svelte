<script lang="ts">
	import messages from '$media/messages.json';
	import { rocketLaunch } from '$lib/components/Svg/outline/rocket-launch.js';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { type FormInputValue } from '$lib/utils/validation';
	import { fade, draw } from 'svelte/transition';
	import { createEventDispatcher, onMount } from 'svelte';
	import { dispatchEventFunctionFactory, type EventDispatchTypeMap } from './shared';

	const dispatch = createEventDispatcher<EventDispatchTypeMap>();

	interface Props {
		emailInput: FormInputValue<string | undefined>;
		passwordInput: FormInputValue<string | undefined>;
		errorMessage: string | undefined;
	}

	let { emailInput, passwordInput, errorMessage }: Props = $props();

	let visible = $state(false);

	onMount(() => {
		visible = true;
	});

	const { onChange, onSubmit } = dispatchEventFunctionFactory(dispatch);
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="w-full flex-col">
		{#if errorMessage}
			<div class="flex justify-center bg-red-500 p-2 text-white">
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
				id="email-mobile"
				type="email"
				name="email-mobile"
				placeholder={messages['login']['email']['placeholder']}
				error={emailInput.error}
				value={emailInput.value}
				on:change={(e) => onChange(e, 'email')}
			>
				{#snippet label()}
								<Input.Label >{messages['login']['email']['label']}</Input.Label>
							{/snippet}
			</Input>
		</div>

		<div class="m-4">
			<Input
				allowClear
				id="password-mobile"
				type="password"
				name="password-mobile"
				showPasswordToggle={true}
				placeholder={messages['login']['password']['placeholder']}
				error={passwordInput.error}
				value={passwordInput.value}
				on:change={(e) => onChange(e, 'password')}
			>
				{#snippet label()}
								<Input.Label >{messages['login']['password']['label']}</Input.Label>
							{/snippet}
			</Input>
		</div>

		<div class="m-4">
			<Button variant="default" on:click={onSubmit}>
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
	</div>
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
