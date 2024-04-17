<script lang="ts">
	import { goto } from '$app/navigation';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { ServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher } from 'svelte';
	import DisplayListItem from './DisplayListItem.svelte';
	import { Button, Modal, Portal } from 'stwui';

	const libre311Service = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	const dispatch = createEventDispatcher<{
		attributeDeleted: ServiceDefinitionAttribute;
	}>();

	export let sda: ServiceDefinitionAttribute;
	export let serviceCode: number;
	export let groupId: number;

	let showConfirmation = false;

	$: href = `/groups/${groupId}/services/${serviceCode}/attributes/${sda.code}`;

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
	<svelte:fragment slot="text">
		{sda.description}
	</svelte:fragment>
</DisplayListItem>

<Portal>
	{#if showConfirmation}
		<Modal handleClose={toggleShowConfirmation}>
			<Modal.Content slot="content" class="max-h-full w-1/2">
				<Modal.Content.Body slot="body" class="overflow-y-auto">
					<div class="my-4">
						<strong>Do you wish to proceed with deleting this attribute?</strong>
						<div>This cannot be undone.</div>
					</div>

					<div class="grid grid-cols-2 gap-2">
						<Button class="col-span-1" type="ghost" on:click={toggleShowConfirmation}>Cancel</Button
						>
						<Button class="col-span-1" type="danger" on:click={deleteSDA}>Confirm</Button>
					</div>
				</Modal.Content.Body>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>
