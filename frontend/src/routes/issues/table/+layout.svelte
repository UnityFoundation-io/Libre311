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
	import type {
		ServiceRequest,
		ServiceRequestId,
		ServiceRequestStatus
	} from '$lib/services/Libre311/Libre311';
	import { toAbbreviatedTimeStamp } from '$lib/utils/functions';
	import type { Maybe } from '$lib/utils/types';
	import { magnifingGlassIcon } from '$lib/components/Svg/outline/magnifyingGlassIcon';
	import type { ComponentEvents } from 'svelte';
	import Funnel from '$lib/components/Svg/outline/Funnel.svelte';
	import { slide } from 'svelte/transition';
	import { quintOut } from 'svelte/easing';
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';

	const linkResolver = useLibre311Context().linkResolver;
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let isSearchFiltersOpen: boolean = false;
	let statusInput: ServiceRequestStatus[];
	let orderBy: string;

	const priorityOptions: SelectOption[] = [
		{
			value: 'low',
			label: 'Low'
		},
		{
			value: 'medium',
			label: 'Medium'
		},
		{
			value: 'high',
			label: 'High'
		}
	];

	const statusOptions: SelectOption[] = [
		{
			value: 'open',
			label: 'Open'
		},
		{
			value: 'closed',
			label: 'Closed'
		}
	];

	// 14% * 7 = 98%
	const columns: TableColumn[] = [
		{
			column: 'issue_id',
			label: 'Issue ID',
			placement: 'center',
			class: 'w-[14%]'
		},
		{
			column: 'service_name',
			label: 'Service Name',
			placement: 'center',
			class: 'w-[14%]'
		},
		{
			column: 'status',
			label: 'Status',
			placement: 'center',
			class: 'w-[14%]'
		},
		{
			column: 'address',
			label: 'Address',
			placement: 'center',
			class: 'hidden xl:table-cell w-[14%]'
		},
		{
			column: 'created_at',
			label: 'Created',
			placement: 'center',
			class: 'hidden lg:table-cell w-[14%]'
		},
		{
			column: 'last_updated',
			label: 'Last Updated',
			placement: 'center',
			class: 'hidden lg:table-cell w-[14%]'
		},
		{
			column: 'expected_completion',
			label: 'Expected Completion',
			placement: 'center',
			class: 'hidden xl:table-cell w-[14%]'
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

	async function handleFunnelClick() {
		isSearchFiltersOpen = !isSearchFiltersOpen;
	}

	async function handleStatusInput(statusInput: ServiceRequestStatus[]) {
		ctx.applyServiceRequestParams({ status: statusInput }, $page.url);
	}

	$: handleStatusInput(statusInput);
</script>

{#if $serviceRequestsRes.type === 'success'}
	<SideBarMainContentLayout>
		<slot slot="side-bar" />
		<div slot="main-content" class="relative flex h-full flex-col">
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
				<Card.Header slot="header" class="flex items-center justify-end py-3 text-lg font-bold">
					{#if !isSearchFiltersOpen}
						<div
							transition:slide|local={{ delay: 100, duration: 500, easing: quintOut, axis: 'x' }}
							class="mx-3"
						>
							<Input slot="extra" placeholder="#Request ID" on:change={handleSearchInput}>
								<Input.Leading slot="trailing" data={magnifingGlassIcon} />
							</Input>
						</div>
					{:else}
						<div
							transition:slide|local={{ delay: 100, duration: 500, easing: quintOut, axis: 'x' }}
							class="mx-3 flex"
						>
							<div class="mx-1">
								<Select
									name="select-priority"
									placeholder="Priority:"
									multiple
									options={priorityOptions}
								>
									<Select.Options slot="options">
										{#each priorityOptions as option}
											<Select.Options.Option {option} />
										{/each}
									</Select.Options>
								</Select>
							</div>

							<div class="mx-1">
								<Select
									name="select-status"
									placeholder="Status:"
									multiple
									options={statusOptions}
									bind:value={statusInput}
								>
									<Select.Options slot="options">
										{#each statusOptions as option}
											<Select.Options.Option {option} />
										{/each}
									</Select.Options>
								</Select>
							</div>
						</div>
					{/if}

					<button on:click={handleFunnelClick}>
						<Funnel />
					</button>
				</Card.Header>
				<Card.Content slot="content" class="p-0 sm:p-0">
					<div class="issues-table-override">
						<Table class="h-full overflow-hidden rounded-md" {columns}>
							<Table.Header slot="header" {orderBy} />
							<Table.Body slot="body">
								{#each $serviceRequestsRes.value.serviceRequests as item}
									<Table.Body.Row
										id={resolveStyleId(item, $selectedServiceRequestStore)}
										on:click={selectRow(item.service_request_id)}
									>
										<Table.Body.Row.Cell column={0}>
											<div class="flex items-center justify-center">
												{item.service_request_id}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={1}>
											<div class="flex items-center justify-center">
												{item.service_name}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={2}>
											<div class="flex items-center justify-center">
												<Badge type={issueStatus(item)}>
													{item.status}
												</Badge>
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={3}>
											<div class="flex items-center justify-center">
												<p
													class="w-24 overflow-hidden text-ellipsis whitespace-nowrap text-sm 2xl:w-32"
												>
													{item.address}
												</p>
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={4}>
											<div class="flex items-center justify-center">
												{toAbbreviatedTimeStamp(item.requested_datetime)}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={5}>
											<div class="flex items-center justify-center">
												{toAbbreviatedTimeStamp(item.updated_datetime)}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={6}>
											<div class="flex items-center justify-center">
												{#if item.expected_datetime}
													{toAbbreviatedTimeStamp(item.expected_datetime)}
												{:else}
													--
												{/if}
											</div>
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
