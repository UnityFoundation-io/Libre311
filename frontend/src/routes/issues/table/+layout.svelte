<script lang="ts">
	import messages from '$media/messages.json';
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Badge, Card, Input, Table } from 'stwui';
	import type { TableColumn } from 'stwui/types';
	import { page } from '$app/stores';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import {
		useSelectedServiceRequestStore,
		useServiceRequestsContext
	} from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';
	import { goto } from '$app/navigation';
	import type { ServiceRequest, ServiceRequestId } from '$lib/services/Libre311/Libre311';
	import { toTimeStamp } from '$lib/utils/functions';
	import type { Maybe } from '$lib/utils/types';
	import { magnifingGlassIcon } from '$lib/components/Svg/outline/magnifyingGlassIcon';
	import type { ComponentEvents } from 'svelte';

	const linkResolver = useLibre311Context().linkResolver;
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let orderBy: string;

	const columns: TableColumn[] = [
		{
			column: 'issue_id',
			label: 'Issue ID',
			placement: 'left',
			class: 'w-[7.5%]'
		},
		{
			column: 'service_name',
			label: 'Service Name',
			placement: 'left',
			class: 'w-[12.5%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'left',
			class: 'w-[10%]'
		},
		{
			column: 'address',
			label: 'Address',
			placement: 'left',
			class: 'w-[25%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'left',
			class: 'w-[15%]'
		},
		{
			column: 'last_updated',
			label: 'Last Updated',
			placement: 'left',
			class: 'w-[15%]'
		},
		{
			column: 'expected_completion',
			label: 'Expected Completion',
			placement: 'left',
			class: 'w-[15%]'
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

	function resolveStyleId(
		serviceRequest: ServiceRequest,
		selectedServiceRequest: Maybe<ServiceRequest>
	) {
		return serviceRequest.service_request_id === selectedServiceRequest?.service_request_id
			? 'selected'
			: 'item-id';
	}

	async function handleSearchInput(e: ComponentEvents<any>['input']) {
		const target = e.target as HTMLInputElement;

		// Remove non-numeric characters from the input value
		let sanitizedValue = target.value.replace(/\D/g, '');

		if (e.target != null && e.target.value) {
			e.target.value = sanitizedValue;
			const serviceRequestId = Number(e.target.value);

			ctx.applyServiceRequestParams([serviceRequestId], $page.url);
		} else {
			ctx.applyServiceRequestParams({}, $page.url);
		}
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
					<Input slot="extra" placeholder="#Request ID" on:change={handleSearchInput}>
						<Input.Leading slot="trailing" data={magnifingGlassIcon} />
					</Input>
				</Card.Header>
				<Card.Content slot="content" class="p-0 sm:p-0" style="height: calc(100% - 64px);">
					<div class="issues-table-override">
						<Table class="h-full overflow-hidden rounded-md" {columns}>
							<Table.Header slot="header" {orderBy} class="space-x-8" />
							<Table.Body slot="body">
								{#each $serviceRequestsRes.value.serviceRequests as item}
									<Table.Body.Row
										id={resolveStyleId(item, $selectedServiceRequestStore)}
										on:click={selectRow(item.service_request_id)}
									>
										<Table.Body.Row.Cell column={0}>
											{item.service_request_id}
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={1}>
											{item.service_name}
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={2}>
											<Badge type={issueStatus(item)}>
												{item.status}
											</Badge>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={3}>
											<p class="w-40 overflow-hidden text-ellipsis whitespace-nowrap text-sm">
												{item.address}
											</p>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={4}>
											{toTimeStamp(item.requested_datetime)}
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={5}>
											{toTimeStamp(item.updated_datetime)}
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={6}>
											{toTimeStamp(item.expected_datetime)}
										</Table.Body.Row.Cell>
									</Table.Body.Row>
								{/each}
							</Table.Body>
						</Table>
					</div>
				</Card.Content>
			</Card>
		</div>
	</SideBarMainContentLayout>
{/if}

<style>
	.issues-table-override :global(#selected) {
		--tw-bg-opacity: 0.15;
		background-color: hsl(var(--primary) / var(--tw-bg-opacity));
	}

	.issues-table-override :global(#item-id):hover {
		--tw-bg-opacity: 0.15;
		background-color: hsl(var(--primary) / var(--tw-bg-opacity));
	}
</style>
