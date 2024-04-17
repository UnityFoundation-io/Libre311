<script lang="ts">
	import type { Service } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher, type ComponentEvents } from 'svelte';

	import DisplayListItem from './DisplayListItem.svelte';
	import EditListItem from './EditListItem.svelte';
	import { Button, Modal, Portal } from 'stwui';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';

	const libre311 = useLibre311Service();
	const alertError = useLibre311Context().alertError;

	const dispatch = createEventDispatcher<{
		serviceDeleted: Service;
		serviceEdited: Service;
	}>();

	export let service: Service;

	let editing = false;
	let showConfirmation = false;

	async function handleEditService(e: ComponentEvents<EditListItem>['confirm']) {
		try {
			const res = await libre311.editService({ ...service, service_name: e.detail.newText });
			editing = false;
			dispatch('serviceEdited', res);
		} catch (error) {
			alertError(error);
		}
	}

	async function deleteService() {
		try {
			await libre311.deleteService(service);
			dispatch('serviceDeleted', service);
			toggleShowConfirmation();
		} catch (error) {
			alertError(error);
		}
	}

	function toggleEdit() {
		editing = !editing;
	}

	function toggleShowConfirmation() {
		showConfirmation = !showConfirmation;
	}

	const dropDownItems = [
		{
			text: 'Edit',
			action: toggleEdit
		},
		{
			text: 'Delete',
			action: toggleShowConfirmation
		}
	];
</script>

{#if editing}
	<EditListItem
		on:confirm={handleEditService}
		on:close={toggleEdit}
		startingText={service.service_name}
		on:toggleEdit={toggleEdit}
	></EditListItem>
{:else}
	<DisplayListItem
		{dropDownItems}
		href={`/groups/${service.group_id}/services/${service.service_code}`}
	>
		<svelte:fragment slot="text">
			{service.service_name}
		</svelte:fragment>
	</DisplayListItem>
{/if}

<Portal>
	{#if showConfirmation}
		<Modal handleClose={toggleShowConfirmation}>
			<Modal.Content slot="content" class="max-h-full w-1/2">
				<Modal.Content.Body slot="body" class="overflow-y-auto">
					<div class="my-4 flex">
						<strong>Remove Service: &nbsp;</strong>
						{` ${service.service_name}`}
					</div>

					<div class="grid grid-cols-2 gap-2">
						<Button class="col-span-1" type="ghost" on:click={toggleShowConfirmation}>Cancel</Button
						>
						<Button class="col-span-1" type="danger" on:click={deleteService}>Confirm</Button>
					</div>
				</Modal.Content.Body>
			</Modal.Content>
		</Modal>
	{/if}
</Portal>
