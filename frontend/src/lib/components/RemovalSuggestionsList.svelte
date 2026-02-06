<script lang="ts">
	import { onMount } from 'svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import type { ServiceRequestRemovalSuggestion } from '$lib/services/Libre311/Libre311';
	import { Button, Card, List } from 'stwui';
	import { toTimeStamp } from '$lib/utils/functions';
	import ConfirmationModal from './ConfirmationModal.svelte';

	export let serviceRequestId: number;

	const libre311Service = useLibre311Service();
	const { refresh } = useServiceRequestsContext();

	let suggestions: ServiceRequestRemovalSuggestion[] = [];
	let showConfirmationModal = false;
	let suggestionIdToDelete: number | null = null;
	let isDeleting = false;

	async function fetchSuggestions(id: number) {
		try {
			suggestions = await libre311Service.getRemovalSuggestions(id);
		} catch (e) {
			console.error('Failed to fetch removal suggestions', e);
		}
	}

	function dismissSuggestion(id: number) {
		suggestionIdToDelete = id;
		showConfirmationModal = true;
	}

	async function confirmDismiss() {
		if (!suggestionIdToDelete) return;
		isDeleting = true;
		try {
			await libre311Service.deleteRemovalSuggestion({
				id: suggestionIdToDelete
			});
			suggestions = suggestions.filter((s) => s.id !== suggestionIdToDelete);
			await refresh();
			showConfirmationModal = false;
		} catch (e) {
			console.error('Failed to dismiss suggestion', e);
			alert('Failed to dismiss suggestion');
		} finally {
			isDeleting = false;
			suggestionIdToDelete = null;
		}
	}

	$: fetchSuggestions(serviceRequestId);

</script>

{#if suggestions.length > 0}
	<Card class="m-2 mt-4 overflow-y-auto">
		<div class="flex h-full w-full flex-col p-2" slot="content">
			<h3 class="mb-2 text-lg font-bold">Removal Suggestions</h3>
			<List>
				{#each suggestions as suggestion (suggestion.id)}
					<List.Item>
						<div class="flex w-full flex-col gap-1">
							<div class="flex items-start justify-between">
								<span class="text-sm font-semibold">{suggestion.email}</span>
								<span class="text-xs text-gray-500">{toTimeStamp(suggestion.date_created)}</span>
							</div>
							{#if suggestion.name}
								<span class="text-xs">{suggestion.name}</span>
							{/if}
							{#if suggestion.phone}
								<span class="text-xs">{suggestion.phone}</span>
							{/if}
							<p class="mt-1 text-sm">{suggestion.reason}</p>
							<div class="mt-2 flex justify-end">
								<Button size="xs" type="danger" on:click={() => dismissSuggestion(suggestion.id)}>
									Dismiss
								</Button>
							</div>
						</div>
					</List.Item>
				{/each}
			</List>
		</div>
	</Card>

	<ConfirmationModal
		open={showConfirmationModal}
		title="Dismiss Suggestion"
		message="Are you sure you want to dismiss this removal suggestion?"
		handleClose={() => (showConfirmationModal = false)}
		handleConfirm={confirmDismiss}
		loading={isDeleting}
	/>
{/if}
