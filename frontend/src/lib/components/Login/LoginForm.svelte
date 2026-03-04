<script lang="ts">
	import messages from '$media/messages.json';
	import { Button, Input } from 'stwui';
	import { createEventDispatcher, onMount, tick } from 'svelte';
	import type { FormInputValue } from '$lib/utils/validation';
	import { setUpAlertRole } from '$lib/utils/functions';

	const dispatch = createEventDispatcher<{
		inputChange: { type: 'email' | 'password'; value: string | undefined };
		login: undefined;
		cancel: undefined;
	}>();

	export let emailInput: FormInputValue<string | undefined>;
	export let passwordInput: FormInputValue<string | undefined>;
	export let errorMessage: string | undefined;
	export let loading = false;
	export let showCancel = true;

	let emailRoot: HTMLElement;
	let passwordRoot: HTMLElement;

	// pass svelte checks
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const passwordAutocomplete = 'current-password' as any;
	// eslint-disable-next-line @typescript-eslint/no-explicit-any
	const emailAutocomplete = 'username' as any;

	function onChange(e: Event, type: 'email' | 'password') {
		const target = e.target as HTMLInputElement;
		dispatch('inputChange', { type, value: target.value });
	}

	function passwordEntered(e: CustomEvent<KeyboardEvent>) {
		const ke = e as unknown as KeyboardEvent;
		if (ke.key === 'Enter') {
			ke.preventDefault();
			dispatch('login');
		}
	}

	$: setUpAlertRole(emailInput, emailRoot, 'input#email-input', 'email-error');
	$: setUpAlertRole(passwordInput, passwordRoot, 'input#password-input', 'password-error');

	onMount(() => {
		tick().then(() => {
			const toggle = passwordRoot.querySelector(
				'.stwui-input-password-toggle-wrapper'
			) as HTMLElement | null;
			if (toggle) {
				toggle.setAttribute('tabindex', '0');
				toggle.setAttribute('role', 'button');
				toggle.setAttribute('aria-label', 'Toggle password visibility');

				toggle.addEventListener('keydown', (e) => {
					if (e.key === 'Enter' || e.key === ' ') {
						e.preventDefault();
						toggle.click();
					}
				});
			}
		});
	});

	export function focus() {
		tick().then(() => {
			emailRoot.querySelector('input')?.focus();
		});
	}
</script>

<div class="flex flex-col gap-4">
	{#if errorMessage}
		<div role="alert" class="flex justify-center rounded-md bg-red-500 p-2 text-white">
			<span>{errorMessage}</span>
		</div>
	{/if}

	<div bind:this={emailRoot}>
		<Input
			allowClear
			id="email-input"
			type="email"
			name="username"
			placeholder={messages['login']['email']['placeholder']}
			error={emailInput.error}
			value={emailInput.value}
			autocomplete={emailAutocomplete}
			on:change={(e) => onChange(e, 'email')}
		>
			<Input.Label slot="label">{messages['login']['email']['label']}</Input.Label>
		</Input>
	</div>

	<div bind:this={passwordRoot}>
		<Input
			allowClear
			id="password-input"
			type="password"
			name="password"
			showPasswordToggle={true}
			placeholder={messages['login']['password']['placeholder']}
			error={passwordInput.error}
			value={passwordInput.value}
			autocomplete={passwordAutocomplete}
			on:keyup={passwordEntered}
			on:change={(e) => onChange(e, 'password')}
		>
			<Input.Label slot="label">{messages['login']['password']['label']}</Input.Label>
		</Input>
	</div>

	<div class="flex gap-2">
		<Button type="primary" on:click={() => dispatch('login')} {loading} class="flex-1">
			{messages['login']['submit']}
		</Button>
		{#if showCancel}
			<Button type="default" on:click={() => dispatch('cancel')}>
				{messages['login']['cancel']}
			</Button>
		{/if}
	</div>
</div>
