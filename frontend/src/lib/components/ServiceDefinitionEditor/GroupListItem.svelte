<script lang="ts">
	import type { Group } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher, type ComponentEvents } from 'svelte';

	import DisplayListItem from './DisplayListItem.svelte';
	import EditListItem from './EditListItem.svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';

	const dispatch = createEventDispatcher<{ editSuccess: Group }>();
	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	let editing = $state(false);

	interface Props {
		group: Group;
	}

	let { group }: Props = $props();

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
		{#snippet text()}
			
				{group.name}
			
			{/snippet}
	</DisplayListItem>
{/if}
