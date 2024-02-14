<script lang="ts">
	import messages from '$media/messages.json';
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';
	import { goto } from '$app/navigation';
	import type { ServiceRequest } from '$lib/services/Libre311/Libre311';

	const linkResolver = useLibre311Context().linkResolver;

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	console.log($serviceRequestsRes);

	let isDrawerOpen = false;

	let orderBy: string;

	const columns: TableColumn[] = [
		{
			column: 'service_name',
			label: 'Service Name',
			placement: 'left',
			class: 'w-[15%]'
			// class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left',
			class: 'w-[10%]'
			// class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'address',
			label: 'Address',
			placement: 'left',
			class: 'w-[50%]'
			// class: 'hidden md:table-cell w-0 md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'right',
			class: 'w-[30%]'
			// class: 'w-[20%] md:w-[10%] lg:w-[20%]'
		}
	];

	function openDrawer(item: ServiceRequest) {
		isDrawerOpen = true;
		goto(`/issues/table/${item.service_request_id}`); // TODO
	}

	function closeDrawer() {
		isDrawerOpen = false;
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

			<Card bordered={false}>
				<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
					Card Header
					<Input slot="extra" />
				</Card.Header>
				<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
					<Table class="h-full overflow-hidden rounded-md" {columns}>
						<Table.Header slot="header" {orderBy} class="space-x-8" />
						<Table.Body slot="body">
							{#each $serviceRequestsRes.value.serviceRequests as item}
								<Table.Body.Row id="item-id" on:click={openDrawer(item)}>
									<Table.Body.Row.Cell class="w-[15%]" column={0}
										>{item.service_name}</Table.Body.Row.Cell
									>
									<Table.Body.Row.Cell class="w-[10%]" column={1}>{item.status}</Table.Body.Row.Cell
									>
									<Table.Body.Row.Cell class="w-[50%]" column={2}
										>{item.address}</Table.Body.Row.Cell
									>
									<Table.Body.Row.Cell class="w-[30%]" column={3}
										>{item.requested_datetime}</Table.Body.Row.Cell
									>
								</Table.Body.Row>
							{/each}
						</Table.Body>
					</Table>
				</Card.Content>
			</Card>
		</div>
	</SideBarMainContentLayout>
{/if}
