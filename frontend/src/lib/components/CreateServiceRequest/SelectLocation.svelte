<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import CancelButton from './CancelButton.svelte';
	import { Button } from 'stwui';

	const dispatch = createEventDispatcher<{
		confirmLocation: undefined;
	}>();
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="grid grid-rows-2 gap-3">
		<h4>Select the location of the Request</h4>
		<Button type="primary" on:click={() => dispatch('confirmLocation')}>Confirm Location</Button>
		<CancelButton />
	</div>
</div>
