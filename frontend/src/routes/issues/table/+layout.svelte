<script lang="ts" context="module">
	let cachedServiceList: GetServiceListResponse | undefined = undefined;
</script>

<script lang="ts">
	import messages from '$media/messages.json';
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { Badge, Button, Card, DatePicker, Input, Table } from 'stwui';
	import { page } from '$app/stores';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import {
		useSelectedServiceRequestStore,
		useServiceRequestsContext
	} from '$lib/context/ServiceRequestsContext';
	import Pagination from '$lib/components/Pagination.svelte';
	import { goto } from '$app/navigation';
	import { saveAs } from 'file-saver';
	import { arrowDownTray } from '$lib/components/Svg/outline/arrowDownTray';
	import type {
		GetServiceListResponse,
		ServiceCode,
		ServiceRequest,
		ServiceRequestId,
		ServiceRequestStatus
	} from '$lib/services/Libre311/Libre311';
	import { toAbbreviatedTimeStamp } from '$lib/utils/functions';
	import type { Maybe } from '$lib/utils/types';
	import { magnifingGlassIcon } from '$lib/components/Svg/outline/magnifyingGlassIcon';
	import { onMount, type ComponentEvents } from 'svelte';
	import Funnel from '$lib/components/Svg/outline/Funnel.svelte';
	import { slide } from 'svelte/transition';
	import { Select } from 'stwui';
	import { columns, priorityOptions, statusOptions } from './table';
	import { calendarIcon } from '$lib/components/Svg/outline/calendarIcon';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import type { SelectOption } from 'stwui/types';

	const linkResolver = useLibre311Context().linkResolver;
	const selectedServiceRequestStore = useSelectedServiceRequestStore();
	const ctx = useServiceRequestsContext();
	const libre311 = useLibre311Service();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServiceCode: ServiceCode[] | undefined;
	let isSearchFiltersOpen: boolean = false;
	let statusInput: ServiceRequestStatus[];
	let orderBy: string;
	let startDate: Date;
	let endDate: Date;

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

	async function handleDownloadCsv() {
		const allServiceRequests = await libre311.getAllServiceRequests({});

		// Sanatize Requests
		for (let request of allServiceRequests) {
			request.address = request.address.replace(/,/g, '');
		}

		const csvContent = convertToCSV(allServiceRequests);

		const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' });
		saveAs(blob, 'service-requests.csv');
	}

	function convertToCSV(data: ServiceRequest[]) {
		const delimiter = '\t';
		const header = Object.keys(data[0]).join(delimiter);

		// Iterate over each object in the data array
		const rows = data
			.map((obj) => {
				// Map over each key in the header
				return header
					.split(delimiter)
					.map((key) => {
						// If the object has a value for the current key
						if (
							obj[key as keyof typeof obj] !== undefined &&
							obj[key as keyof typeof obj] !== null
						) {
							// If the value is an object, stringify it as JSON
							if (typeof obj[key as keyof typeof obj] === 'object') {
								// Ugly looking JSON (service definition answers)
								return JSON.stringify(obj[key as keyof typeof obj]);
							} else {
								// Otherwise, return the value as is
								return obj[key as keyof typeof obj];
							}
						} else {
							// If the value is undefined or null, return an empty string
							return '';
						}
					})
					.join(delimiter);
			})
			.join('\n');

		return `${header}\n${rows}`;
	}

	async function handleFunnelClick() {
		isSearchFiltersOpen = !isSearchFiltersOpen;
	}

	async function handleFilterInput(
		selectedServiceCode: ServiceCode[] | undefined,
		statusInput: ServiceRequestStatus[],
		startDate: Date,
		endDate: Date
	) {
		ctx.applyServiceRequestParams(
			{
				serviceCode: selectedServiceCode,
				status: statusInput,
				startDate: startDate?.toISOString(),
				endDate: endDate?.toISOString()
			},
			$page.url
		);
		console.log(ctx.serviceRequestsResponse);
	}

	onMount(fetchServiceList);

	$: handleFilterInput(selectedServiceCode, statusInput, startDate, endDate);
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

			<div
				class="border-border m-3 flex items-center justify-end rounded-md border-t-[1px] shadow-md"
			>
				<div class="m-3 flex items-center">
					{#if !isSearchFiltersOpen}
						<div transition:slide|local={{ duration: 500 }}>
							<Input slot="extra" placeholder="#Request ID" on:change={handleSearchInput}>
								<Input.Leading slot="trailing" data={magnifingGlassIcon} />
							</Input>
						</div>
					{:else}
						<div class="flex flex-wrap justify-end" transition:slide|local={{ duration: 500 }}>
							<div class="m-1">
								<Select
									name="select-priority"
									placeholder="Priority:"
									multiple
									options={priorityOptions}
								>
									<Select.Label slot="label">Priority</Select.Label>
									<Select.Options slot="options">
										{#each priorityOptions as option}
											<Select.Options.Option {option} />
										{/each}
									</Select.Options>
								</Select>
							</div>

							<div class="m-1">
								<Select
									name="select-status"
									placeholder="Status:"
									multiple
									options={statusOptions}
									bind:value={statusInput}
								>
									<Select.Label slot="label">Status</Select.Label>
									<Select.Options slot="options">
										{#each statusOptions as option}
											<Select.Options.Option {option} />
										{/each}
									</Select.Options>
								</Select>
							</div>

							{#if serviceList.type === 'success'}
								{@const selectOptions = createSelectOptions(serviceList.value)}
								<div class="m-1 min-w-52">
									<Select
										bind:value={selectedServiceCode}
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
									<DatePicker.Label slot="label">Start Date</DatePicker.Label>
									<DatePicker.Leading slot="leading" data={calendarIcon} />
								</DatePicker>
							</div>

							<div class="m-1">
								<DatePicker name="end-datetime" allowClear bind:value={endDate}>
									<DatePicker.Label slot="label">End Date</DatePicker.Label>
									<DatePicker.Leading slot="leading" data={calendarIcon} />
								</DatePicker>
							</div>
						</div>
					{/if}
				</div>

				<button class="mr-3" on:click={handleFunnelClick}>
					<Funnel />
				</button>
			</div>

			<Card bordered={true} class="m-2">
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
												<Badge type={issueStatus(item)}>
													{item.status}
												</Badge>
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
												{toAbbreviatedTimeStamp(item.updated_datetime)}
											</div>
										</Table.Body.Row.Cell>

										<Table.Body.Row.Cell column={7}>
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
	</SideBarMainContentLayout>
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
