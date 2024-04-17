<script lang="ts">
	import type { Group } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher, type ComponentEvents } from 'svelte';

	import DisplayListItem from './DisplayListItem.svelte';
	import EditListItem from './EditListItem.svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';

	const dispatch = createEventDispatcher<{ editSuccess: Group }>();
	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let editing = false;

	export let group: Group;

	async function handleConfirm(e: ComponentEvents<EditListItem>['confirm']) {
		try {
			const res = await libre311.editGroup({ id: group.id, name: e.detail.newText });
			dispatch('editSuccess', res);
			editing = false;
		} catch (error) {
			alertError(error);
		}
	}

	function toggle() {
		editing = !editing;
	}

	const dropDownItems = [
		{
			text: 'Edit',
			action: toggle
		}
	];
</script>

{#if editing}
	<EditListItem
		on:confirm={handleConfirm}
		on:close={toggle}
		startingText={group.name}
		on:toggleEdit={toggle}
	></EditListItem>
{:else}
	<DisplayListItem {dropDownItems} href={`/groups/${group.id}`} on:toggleEdit={toggle}>
		<svelte:fragment slot="text">
			{group.name}
		</svelte:fragment>
	</DisplayListItem>
{/if}
