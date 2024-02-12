<script lang="ts">
	import { Badge, Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { Drawer, Portal } from 'stwui';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';

	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import Pagination from './Pagination.svelte';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	const linkResolver = useLibre311Context().linkResolver;

	let drawerLeftOpen = false;

	const columns: TableColumn[] = [
		{
			column: 'project_name',
			label: 'Project Name',
			placement: 'left',
			class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left',
			class: 'w-[40%] md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'city',
			label: 'City',
			placement: 'left',
			class: 'hidden md:table-cell w-0 md:w-[30%] lg:w-[20%]'
		},
		{
			column: 'state',
			label: 'State',
			placement: 'left',
			class: 'hidden lg:table-cell lg:w-[20%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'right',
			class: 'w-[20%] md:w-[10%] lg:w-[20%]'
		}
	];

	function openDrawerLeft() {
		drawerLeftOpen = true;
	}

	function closeDrawerLeft() {
		drawerLeftOpen = false;
	}
</script>

{#if $serviceRequestsRes.type === 'success'}
	<Portal>
		{#if drawerLeftOpen}
			<Drawer handleClose={closeDrawerLeft} placement="left">
				<Drawer.Content slot="content">Drawer Content</Drawer.Content>
			</Drawer>
		{/if}
	</Portal>

	<Card bordered={false} class="h-[calc(100vh-14rem)]">
		<Card.Header slot="header" class="flex items-center justify-between py-3 text-lg font-bold">
			Card Header
			<Input slot="extra" />
		</Card.Header>
		<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
			<Table class="h-full overflow-hidden rounded-md" {columns}>
				<Table.Body slot="body">
					{#each $serviceRequestsRes.value.serviceRequests as item}
						<Table.Body.Row id="item.id" on:click={openDrawerLeft}>
							<Table.Body.Row.Cell column={0}>{item.service_name}</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={1}>{item.status}</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={2}>{item.address}</Table.Body.Row.Cell>
							<Table.Body.Row.Cell column={4}>{item.requested_datetime}</Table.Body.Row.Cell>
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
{/if}
