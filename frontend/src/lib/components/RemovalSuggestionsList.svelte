<script lang="ts">
	import { onMount } from 'svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
    import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import type { ServiceRequestRemovalSuggestion } from '$lib/services/Libre311/Libre311';
	import { Button, Card, List } from 'stwui';
	import { toTimeStamp } from '$lib/utils/functions';
    import messages from '$media/messages.json';
    import ConfirmationModal from './ConfirmationModal.svelte';

	export let serviceRequestId: number;

	const libre311Service = useLibre311Service();
    const { refresh } = useServiceRequestsContext();

	let suggestions: ServiceRequestRemovalSuggestion[] = [];
	let loading = false;
    let showConfirmationModal = false;
    let suggestionIdToDelete: number | null = null;
    let isDeleting = false;

	async function fetchSuggestions() {
		loading = true;
		try {
			const res = await libre311Service.getRemovalSuggestions({
				pagination: {
                    pageNumber: 0,
                    size: 100, // Fetch enough to show all likely suggestions for one request
                    offset: 0,
                    totalPages: 0,
                    totalSize: 0
                },
				// @ts-ignore - jurisdiction_id is injected by service
				jurisdiction_id: '',
                service_request_id: serviceRequestId
			});
			suggestions = res.content;
		} catch (e) {
			console.error('Failed to fetch removal suggestions', e);
		} finally {
			loading = false;
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
                id: suggestionIdToDelete,
                // @ts-ignore
                jurisdiction_id: ''
            });
            suggestions = suggestions.filter(s => s.id !== suggestionIdToDelete);
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

	onMount(() => {
		fetchSuggestions();
	});
</script>

{#if suggestions.length > 0}
    <Card class="m-2 overflow-y-auto mt-4">
        <div class="flex h-full w-full flex-col p-2" slot="content">
            <h3 class="text-lg font-bold mb-2">Removal Suggestions</h3>
            <List>
                {#each suggestions as suggestion}
                    <List.Item>
                        <div class="flex flex-col w-full gap-1">
                            <div class="flex justify-between items-start">
                                <span class="font-semibold text-sm">{suggestion.email}</span>
                                <span class="text-xs text-gray-500">{toTimeStamp(suggestion.date_created)}</span>
                            </div>
                            {#if suggestion.name}
                                <span class="text-xs">{suggestion.name}</span>
                            {/if}
                            {#if suggestion.phone}
                                <span class="text-xs">{suggestion.phone}</span>
                            {/if}
                            <p class="text-sm mt-1">{suggestion.reason}</p>
                            <div class="flex justify-end mt-2">
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
