<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher } from 'svelte';
	import DisplayListItem from './DisplayListItem.svelte';
	import { Modal, Portal } from 'stwui';
    import {Button} from "$lib/components/ui/button";

	const libre311Service = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	const dispatch = createEventDispatcher<{
		attributeDeleted: ServiceDefinitionAttribute;
	}>();

	interface Props {
		sda: ServiceDefinitionAttribute;
		serviceCode: number;
		groupId: number;
	}

	let { sda, serviceCode, groupId }: Props = $props();

	let showConfirmation = $state(false);

	let href = $derived(`/groups/${groupId}/services/${serviceCode}/attributes/${sda.code}`);

	function toggleShowConfirmation() {
		showConfirmation = !showConfirmation;
	}

	async function deleteSDA() {
		try {
			await libre311Service.deleteAttribute({ serviceCode, attributeCode: sda.code });
			dispatch('attributeDeleted', sda);
			toggleShowConfirmation();
		} catch (e: unknown) {
			alertError(e);
		}
	}

	const dropDownItems = [
		{
			text: 'Edit',
			action: () => goto(href)
		},
		{
			text: 'Delete',
			action: toggleShowConfirmation
		}
	];
</script>

<DisplayListItem {dropDownItems} {href}>
	{#snippet text()}
	
			{sda.description}
		
	{/snippet}
</DisplayListItem>

<Portal>
	{#if showConfirmation}
		<Modal handleClose={toggleShowConfirmation}>
			{#snippet content()}
						<Modal.Content  class="max-h-full w-1/2">
					{#snippet body()}
								<Modal.Content.Body  class="overflow-y-auto">
							<div class="my-4">
								<strong>Do you wish to proceed with deleting this attribute?</strong>
								<div>This cannot be undone.</div>
							</div>

							<div class="grid grid-cols-2 gap-2">
								<Button class="col-span-1" variant="ghost" on:click={toggleShowConfirmation}>Cancel</Button
								>
								<Button class="col-span-1" variant="destructive" on:click={deleteSDA}>Confirm</Button>
							</div>
						</Modal.Content.Body>
							{/snippet}
				</Modal.Content>
					{/snippet}
		</Modal>
	{/if}
</Portal>
