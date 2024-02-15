<script lang="ts">
	import messages from '$media/messages.json';
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Badge, Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';
	import { goto } from '$app/navigation';
	import type { ServiceRequest, ServiceRequestId } from '$lib/services/Libre311/Libre311';
	import { toTimeStamp } from '$lib/utils/functions';

	const linkResolver = useLibre311Context().linkResolver;

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let orderBy: string;

	const columns: TableColumn[] = [
		{
			column: 'service_name',
			label: 'Service Name',
			placement: 'left'
			// class: 'w-[10%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left'
			// class: 'w-[10%]'
		},
		{
			column: 'address',
			label: 'Address',
			placement: 'left'
			// class: 'w-[50%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'left'
			// class: 'w-[30%]'
		}
	];

	function selectRow(service_request_id: ServiceRequestId) {
		goto(`/issues/table/${service_request_id}`);
		return;
	}

	function issueStatus(item: ServiceRequest): 'warn' | 'success' {
		if (item.status === 'open') return 'warn';
		else return 'success';
	}
</script>

{#if $serviceRequestsRes.type === 'success'}
	<SideBarMainContentLayout>
		<slot slot="side-bar" />
		<div slot="main-content" class="relative h-full flex-col">
			<div class="m-3 flex items-center justify-between">
				<div>
					<p class="text-base">{messages['sidebar']['title']}</p>
				</div>

				<div>
					<Pagination
						pagination={$serviceRequestsRes.value.metadata.pagination}
						nextPage={linkResolver.nextIssuesPage(
							$serviceRequestsRes.value.metadata.pagination,
							$page.url
						)}
						prevPage={linkResolver.prevIssuesPage(
							$serviceRequestsRes.value.metadata.pagination,
							$page.url
						)}
					/>
				</div>
			</div>

			<Card bordered={true} class="m-2">
				<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
					Card Header
					<Input slot="extra" />
				</Card.Header>
				<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
					<Table class="h-full overflow-hidden rounded-md" {columns}>
						<Table.Header slot="header" {orderBy} class="space-x-8" />
						<Table.Body slot="body">
							{#each $serviceRequestsRes.value.serviceRequests as item}
								<Table.Body.Row id="item-id" on:click={selectRow(item.service_request_id)}>
									<Table.Body.Row.Cell column={0}>
										{item.service_name}
									</Table.Body.Row.Cell>

									<Table.Body.Row.Cell column={1}>
										<Badge type={issueStatus(item)}>
											{item.status}
										</Badge>
									</Table.Body.Row.Cell>

									<Table.Body.Row.Cell column={2}>
										<p class="w-40 overflow-hidden text-ellipsis whitespace-nowrap text-sm">
											{item.address}
										</p>
									</Table.Body.Row.Cell>

									<Table.Body.Row.Cell column={3}>
										{toTimeStamp(item.requested_datetime)}
									</Table.Body.Row.Cell>
								</Table.Body.Row>
							{/each}
						</Table.Body>
					</Table>
				</Card.Content>
			</Card>
		</div>
	</SideBarMainContentLayout>
{/if}
