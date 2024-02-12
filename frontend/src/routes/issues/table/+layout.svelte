<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';
	import { goto } from '$app/navigation';

	const linkResolver = useLibre311Context().linkResolver;

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let isDrawerOpen = false;

	const columns: TableColumn[] = [
		{
			column: 'project_name',
			label: 'Project Name',
			placement: 'left'
			// class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left'
			// class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'city',
			label: 'City',
			placement: 'left'
			// class: 'hidden md:table-cell w-0 md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'right'
			// class: 'w-[20%] md:w-[10%] lg:w-[20%]'
		}
	];

	function openDrawer() {
		isDrawerOpen = true;
		goto(`/issues/table/1`); // TODO
	}

	function closeDrawer() {
		isDrawerOpen = false;
	}
</script>

{#if $serviceRequestsRes.type === 'success'}
	<SideBarMainContentLayout>
		<slot slot="side-bar" />
		<div slot="main-content" class="relative h-full flex-col">
			<Card bordered={false}>
				<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
					Card Header
					<Input slot="extra" />
				</Card.Header>
				<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
					<Table {columns}>
						<Table.Body slot="body">
							{#each $serviceRequestsRes.value.serviceRequests as item}
								<Table.Body.Row id="item-id" on:click={openDrawer}>
									<Table.Body.Row.Cell column={0}>{item.service_name}</Table.Body.Row.Cell>
									<Table.Body.Row.Cell column={1}>{item.status}</Table.Body.Row.Cell>
									<Table.Body.Row.Cell column={2}>{item.address}</Table.Body.Row.Cell>
									<!-- <Table.Body.Row.Cell column={3}>{item.requested_datetime}</Table.Body.Row.Cell> -->
								</Table.Body.Row>
							{/each}
						</Table.Body>
					</Table>
				</Card.Content>
			</Card>

			<div class="flex justify-end">
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
	</SideBarMainContentLayout>
{/if}
