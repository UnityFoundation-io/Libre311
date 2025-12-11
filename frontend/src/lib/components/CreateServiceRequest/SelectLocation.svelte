<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import CancelButton from './CancelButton.svelte';
    import {Button} from "$lib/components/ui/button";

	interface Props {
		loading?: boolean | undefined;
	}

	let { loading = undefined }: Props = $props();

	const dispatch = createEventDispatcher<{
		confirmLocation: undefined;
	}>();
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="grid grid-rows-2 gap-3">
		<h4>Select the location of the Request</h4>
		<Button {loading} on:click={() => dispatch('confirmLocation')}
			>Confirm Location</Button
		>
		<CancelButton />
	</div>
	<div class="absolute inset-x-0 bottom-0 bg-gray-100 text-xs">
		This site is protected by reCAPTCHA and the Google.
		<a class="text-[#2563eb]" href="https://policies.google.com/privacy">Privacy Policy</a> and
		<a class="text-[#2563eb]" href="https://policies.google.com/terms">Terms of Service</a> apply.
	</div>
</div>
