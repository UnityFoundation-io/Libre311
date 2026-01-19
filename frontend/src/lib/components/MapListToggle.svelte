<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { MapOrList, type MapOrListToggle } from './map_or_list_toggle';

	export let toggled: MapOrListToggle = MapOrList.Map;

	const dispatch = createEventDispatcher<{
		change: MapOrListToggle;
	}>();

	function set(value: MapOrListToggle) {
		toggled = value;
		dispatch('change', value);
	}
</script>

<div class="toggle">
	<button
		class="rounded-l-lg"
		class:selected={toggled === MapOrList.List}
		on:click={() => set(MapOrList.List)}>List</button
	>
	<button
		class="rounded-r-lg"
		class:selected={toggled === MapOrList.Map}
		on:click={() => set(MapOrList.Map)}>Map</button
	>
</div>

<style>
	.toggle {
		display: flex;
	}

	button {
		padding: 0.5rem 2rem;
		background-color: hsl(var(--surface));
		color: hsl(var(--content));
		border: 1px solid hsl(var(--primary));
		position: relative;
	}

	button:hover::before {
		--tw-surface-opacity: 0.05;
		top: 0;
		bottom: 0;
		left: 0;
		right: 0;
		content: '';
		position: absolute;
		opacity: var(--tw-surface-opacity);
		background-color: hsl(var(--hover));
	}

	.selected {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
	}
</style>
