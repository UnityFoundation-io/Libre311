<script lang="ts">
	import { onMount } from 'svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		ServiceRequestId,
		ServiceRequestRemovalSuggestion
	} from '$lib/services/Libre311/Libre311';
	import { Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { toTimeStamp } from '$lib/utils/functions';
	import Pagination from '$lib/components/Pagination.svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';

	const libre311Service = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;

	let suggestions: ServiceRequestRemovalSuggestion[] = [];
	let loading = false;
	let pagination = {
		pageNumber: 0,
		size: 10,
		totalSize: 0,
		totalPages: 0,
		offset: 0
	};

	const columns: TableColumn[] = [
		{
			column: 'id',
			label: 'ID',
			placement: 'left',
			class: 'w-[5%]'
		},
		{
			column: 'service_request_id',
			label: 'Request ID',
			placement: 'left',
			class: 'w-[10%]'
		},
		{
			column: 'reason',
			label: 'Reason',
			placement: 'left',
			class: 'w-[30%]'
		},
		{
			column: 'email',
			label: 'Contact',
			placement: 'left',
			class: 'w-[25%]'
		},
		{
			column: 'date_created',
			label: 'Date',
			placement: 'left',
			class: 'w-[15%]'
		},
		{
			column: 'actions',
			label: '',
			placement: 'right',
			class: 'w-[15%]'
		}
	];

	async function fetchSuggestions(page: number = 0) {
		loading = true;
		try {
			// jurisdiction_id is handled by the service implementation using the config
			const res = await libre311Service.getRemovalSuggestions({
				pagination: { ...pagination, pageNumber: page },
				// @ts-ignore - jurisdiction_id is injected by service
				jurisdiction_id: ''
			});
			suggestions = res.content;
			pagination = res.metadata.pagination;
		} catch (e) {
			console.error('Failed to fetch removal suggestions', e);
		} finally {
			loading = false;
		}
	}

	function selectRow(service_request_id: ServiceRequestId) {
		goto(linkResolver.issueDetailsTable($page.url, service_request_id));
		return;
	}

	function handlePageChange(event: CustomEvent<number>) {
		fetchSuggestions(event.detail);
	}

	onMount(() => {
		fetchSuggestions();
	});
</script>

<div class="h-full flex flex-col overflow-hidden">
	<Table class="h-full overflow-hidden rounded-md" {columns}>
		<Table.Header slot="header" />
		<Table.Body slot="body">
			{#each suggestions as suggestion}
				<Table.Body.Row on:click={() => selectRow(suggestion.service_request_id)}>
					<Table.Body.Row.Cell column={0}>
						{suggestion.id}
					</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={1}>
                        {suggestion.service_request_id}
					</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={2}>
						<div class="max-w-xs truncate" title={suggestion.reason}>
							{suggestion.reason}
						</div>
					</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={3}>
						<div class="flex flex-col">
							<span>{suggestion.email}</span>
							{#if suggestion.name}<span class="text-xs">{suggestion.name}</span>{/if}
							{#if suggestion.phone}<span class="text-xs">{suggestion.phone}</span>{/if}
						</div>
					</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={4}>
						{toTimeStamp(suggestion.date_created)}
					</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={5}>
						<!-- Future: Add Approve/Reject actions here -->
					</Table.Body.Row.Cell>
				</Table.Body.Row>
			{/each}
			{#if suggestions.length === 0 && !loading}
				<Table.Body.Row>
					<Table.Body.Row.Cell column={0}>No removal suggestions found.</Table.Body.Row.Cell>
					<Table.Body.Row.Cell column={1} />
					<Table.Body.Row.Cell column={2} />
					<Table.Body.Row.Cell column={3} />
					<Table.Body.Row.Cell column={4} />
					<Table.Body.Row.Cell column={5} />
				</Table.Body.Row>
			{/if}
		</Table.Body>
		<Table.Footer slot="footer">
			<div class="p-2 w-full flex justify-end">
				<Pagination {pagination} on:pageChange={handlePageChange} />
			</div>
		</Table.Footer>
	</Table>
</div>


