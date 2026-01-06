<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import TableWithDetailPane from '$lib/components/TableWithDetailPane.svelte';
	import { Button, Card, DatePicker, Input, Table } from 'stwui';
	import { page } from '$app/stores';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import {
		useSelectedServiceRequestStore,
		useServiceRequestsContext
	} from '$lib/context/ServiceRequestsContext';
	import { goto } from '$app/navigation';
	import { saveAs } from 'file-saver';
	import { arrowDownTray } from '$lib/components/Svg/outline/arrowDownTray';
	import {
		type GetServiceListResponse,
		type ServiceRequest,
		type ServiceRequestId,
		type ServiceRequestPriority,
		type ServiceRequestStatus
	} from '$lib/services/Libre311/Libre311';
	import {
		serviceRequestPrioritySelectOptions,
		serviceRequestStatusSelectOptions,
		toAbbreviatedTimeStamp
	} from '$lib/utils/functions';
	import type { Maybe } from '$lib/utils/types';
	import { magnifingGlassIcon } from '$lib/components/Svg/outline/magnifyingGlassIcon';
	import { onMount } from 'svelte';
	import Funnel from '$lib/components/Svg/outline/Funnel.svelte';
	import { slide } from 'svelte/transition';
	import { Select } from 'stwui';
	import { columns } from './table';
	import Pagination from '$lib/components/Pagination.svelte';
	import { calendarIcon } from '$lib/components/Svg/outline/calendarIcon';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import type { SelectOption } from 'stwui/types';
	import ServiceRequestStatusBadge from '$lib/components/ServiceRequestStatusBadge.svelte';
	import { FilteredServiceRequestsParamsMapper } from '$lib/services/Libre311/FilteredServiceRequestsParamsMapper';

	const linkResolver = useLibre311Context().linkResolver;
	const selectedServiceRequestStore = useSelectedServiceRequestStore();
	const ctx = useServiceRequestsContext();
	const libre311 = useLibre311Service();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServicePriority: ServiceRequestPriority[];
	let selectedServiceCodes: string[] | undefined;
	let isSearchFiltersOpen: boolean = false;
	let statusInput: ServiceRequestStatus[];
	let orderBy: string;
	let startDate: Date;
	let endDate: Date;

	function selectRow(service_request_id: ServiceRequestId) {
		goto(linkResolver.issueDetailsTable($page.url, service_request_id));
		return;
	}

	function resolveStyleId(
		serviceRequest: ServiceRequest,
		selectedServiceRequest: Maybe<ServiceRequest>
	) {
		return serviceRequest.service_request_id === selectedServiceRequest?.service_request_id
			? 'selected'
			: 'item-id';
	}

	function fetchServiceList() {
		if (cachedServiceList) {
			serviceList = asAsyncSuccess(cachedServiceList);
			return;
		}
		libre311
			.getServiceList()
			.then((res) => {
				cachedServiceList = res;
				serviceList = asAsyncSuccess(res);
			})
			.catch((err) => (serviceList = asAsyncFailure(err)));
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	async function handleSearchInput(e: Event) {
		const target = e.target as HTMLInputElement;

		// Remove non-numeric characters from the input value
		let sanitizedValue = target.value.replace(/\D/g, '');

		if (sanitizedValue) {
			const serviceRequestId = Number(sanitizedValue);
			ctx.applyServiceRequestParams([serviceRequestId], $page.url);
		} else {
			ctx.applyServiceRequestParams({}, $page.url);
		}
	}

	async function handleDownloadCsv() {
		const serviceRequestsBlob = await libre311.downloadServiceRequests(
			FilteredServiceRequestsParamsMapper.toRequestParams($page.url.searchParams)
		);

		saveAs(serviceRequestsBlob, 'service-requests.csv');
	}

	async function handleFunnelClick() {
		isSearchFiltersOpen = !isSearchFiltersOpen;
	}

	async function handleFilterInput(
		selectedServicePriority: ServiceRequestPriority[],
		selectedServiceCodes: string[] | undefined,
		statusInput: ServiceRequestStatus[],
		startDate: Date,
		endDate: Date
	) {
		// Sets endDate to be one day later than what's selected to play nicely with STWUI
		const oneDayInMilliseconds = 24 * 60 * 60 * 1000; // Number of milliseconds in one day

		if (endDate != undefined) {
			endDate = new Date(endDate.getTime() + oneDayInMilliseconds);
		}

		ctx.applyServiceRequestParams(
			{
				servicePriority: selectedServicePriority,
				serviceCode: selectedServiceCodes?.map((s) => Number(s)),
				status: statusInput,
				startDate: startDate?.toISOString(),
				endDate: endDate?.toISOString()
			},
			$page.url
		);
	}

	onMount(fetchServiceList);

	$: handleFilterInput(
		selectedServicePriority,
		selectedServiceCodes,
		statusInput,
		startDate,
		endDate
	);

	// Detail pane is open when we have an issue_id in the route
	$: detailPaneOpen = Boolean($page.params.issue_id);
</script>

{#if $serviceRequestsRes.type === 'success'}
	<TableWithDetailPane {detailPaneOpen}>
		<div slot="detail-pane">
			<slot />
		</div>
		<div slot="table" class="relative flex h-full flex-col">
			<h3 class="ml-3 mt-2 text-base">Service Requests</h3>
			<div
				class="m-3 flex items-center justify-between rounded-md border-t-[1px] border-border shadow-md"
			>
				<div class="m-3">
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
				<div class="flex items-center">
					<div class="m-3 flex items-center">
						{#if !isSearchFiltersOpen}
							<div transition:slide|local={{ duration: 500 }}>
								<Input class="w-36" placeholder="#Request ID" on:change={handleSearchInput}>
									<Input.Leading slot="trailing" data={magnifingGlassIcon} />
								</Input>
							</div>
						{:else}
							<div class="flex flex-wrap justify-end" transition:slide|local={{ duration: 500 }}>
								<div class="m-1 min-w-32">
									<Select
										bind:value={selectedServicePriority}
										name="select-priority"
										placeholder="Priority:"
										multiple
										options={serviceRequestPrioritySelectOptions}
									>
										<Select.Label slot="label">Priority</Select.Label>
										<Select.Options slot="options">
											{#each serviceRequestPrioritySelectOptions as option}
												<Select.Options.Option {option} />
											{/each}
										</Select.Options>
									</Select>
								</div>

								<div class="m-1 min-w-36">
									<Select
										name="select-status"
										placeholder="Status:"
										multiple
										options={serviceRequestStatusSelectOptions}
										bind:value={statusInput}
									>
										<Select.Label slot="label">Status</Select.Label>
										<Select.Options slot="options">
											{#each serviceRequestStatusSelectOptions as option}
												<Select.Options.Option {option} />
											{/each}
										</Select.Options>
									</Select>
								</div>

								{#if serviceList.type === 'success'}
									{@const selectOptions = createSelectOptions(serviceList.value)}
									<div class="m-1 min-w-52">
										<Select
											bind:value={selectedServiceCodes}
											name="select-1"
											placeholder="Request Type"
											multiple
											options={selectOptions}
										>
											<Select.Label slot="label">Service</Select.Label>
											<Select.Options slot="options">
												{#each selectOptions as option}
													<Select.Options.Option {option} />
												{/each}
											</Select.Options>
										</Select>
									</div>
								{/if}

								<div class="m-1">
									<DatePicker name="start-datetime" allowClear bind:value={startDate}>
										<DatePicker.Label slot="label">Reported From</DatePicker.Label>
										<DatePicker.Leading slot="leading" data={calendarIcon} />
									</DatePicker>
								</div>

								<div class="m-1">
									<DatePicker name="end-datetime" allowClear bind:value={endDate}>
										<DatePicker.Label slot="label">Reported To</DatePicker.Label>
										<DatePicker.Leading slot="leading" data={calendarIcon} />
									</DatePicker>
								</div>
							</div>
						{/if}
					</div>

					<button class="mr-3" title="Filter" aria-label="Filter" on:click={handleFunnelClick}>
						<Funnel />
					</button>
				</div>
			</div>

			<!-- flex-1 allows the table card to fill remaining vertical space in the layout -->
			<Card bordered={true} class="m-2 flex-1 overflow-hidden">
				<Card.Content slot="content" class="h-full p-0 sm:p-0">
					<div class="issues-table-override h-full">
						<Table class="h-full overflow-hidden rounded-md" {columns}>
							<Table.Header slot="header" {orderBy} />
							<Table.Body slot="body">
								{#each $serviceRequestsRes.value.serviceRequests as item}
									<Table.Body.Row
										id={resolveStyleId(item, $selectedServiceRequestStore)}
										on:click={() => selectRow(item.service_request_id)}
									>
										<Table.Body.Row.Cell column={0}>
											<div class="flex items-center justify-center">
												{item.service_request_id}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={1}>
											<div class="flex items-center justify-center">
												{item.priority
													? `${item.priority.charAt(0).toUpperCase()}${item.priority.slice(1)}`
													: '--'}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={2}>
											<div class="flex items-center justify-center">
												{item.service_name}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={3}>
											<div class="flex items-center justify-center">
												<ServiceRequestStatusBadge status={item.status} />
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={4}>
											<div class="flex items-center justify-center">
												<p
													class="w-24 overflow-hidden text-ellipsis whitespace-nowrap text-sm 2xl:w-32"
												>
													{item.address}
												</p>
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={5}>
											<div class="flex items-center justify-center">
												{toAbbreviatedTimeStamp(item.requested_datetime)}
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

							<Table.Footer slot="footer">
								<div class="m-2 flex justify-end">
									<Button on:click={handleDownloadCsv}>
										Download CSV
										<Button.Trailing data={arrowDownTray} slot="trailing" />
									</Button>
								</div>
							</Table.Footer>
						</Table>
					</div>
				</Card.Content>
			</Card>
		</div>
	</TableWithDetailPane>
{:else if $serviceRequestsRes.type === 'failure'}
	{JSON.stringify($serviceRequestsRes, null, 2)}
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
