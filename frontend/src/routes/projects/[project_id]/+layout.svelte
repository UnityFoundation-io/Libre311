<script lang="ts">
	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { Project, ServiceRequest } from '$lib/services/Libre311/Libre311';
	import {
		createServiceRequestsContext,
		useSelectedServiceRequestStore,
	} from '$lib/context/ServiceRequestsContext';
	import TableWithDetailPane from '$lib/components/TableWithDetailPane.svelte';
	import { Card, Table, Button } from 'stwui';
	import { goto } from '$app/navigation';
	import ServiceRequestStatusBadge from '$lib/components/ServiceRequestStatusBadge.svelte';
	import { toAbbreviatedTimeStamp } from '$lib/utils/functions';
	import { arrowDownTray } from '$lib/components/Svg/outline/arrowDownTray';
	import { saveAs } from 'file-saver';
	import { FilteredServiceRequestsParamsMapper } from '$lib/services/Libre311/FilteredServiceRequestsParamsMapper';
	import { columns } from '../../issues/table/table'; // Reusing columns from issues table
	import AuthGuard from '$lib/components/AuthGuard.svelte';

	const libre311 = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;

	let project: Project | undefined;
	let isLoading = true;

	// Create a new ServiceRequestsContext for this project view
	const ctx = createServiceRequestsContext(libre311, page);
	const serviceRequestsRes = ctx.serviceRequestsResponse;
	const selectedServiceRequestStore = useSelectedServiceRequestStore();

	onMount(async () => {
		try {
			const projects = await libre311.getProjects();
			project = projects.find((p) => p.id === Number($page.params.project_id));
		} catch (error) {
			console.error('Failed to load project:', error);
		} finally {
			isLoading = false;
		}
	});

	function selectRow(issue_id: number) {
		goto(`/projects/${$page.params.project_id}/${issue_id}`);
	}

	function resolveStyleId(item: ServiceRequest, selected: ServiceRequest | undefined | null) {
		return item.service_request_id === selected?.service_request_id ? 'selected' : 'item-id';
	}

	async function handleDownloadCsv() {
		const serviceRequestsBlob = await libre311.downloadServiceRequests({
			...FilteredServiceRequestsParamsMapper.toRequestParams($page.url.searchParams),
		});
		saveAs(serviceRequestsBlob, `project-${$page.params.project_id}-requests.csv`);
	}

	$: detailPaneOpen = Boolean($page.params.issue_id);
</script>

<AuthGuard
	requires={[
		'LIBRE311_ADMIN_VIEW-TENANT',
		'LIBRE311_ADMIN_VIEW-SYSTEM',
		'LIBRE311_ADMIN_VIEW-SUBTENANT'
	]}
>
	<div class="flex h-full flex-col">
		{#if project}
			<div class="bg-white p-4 shadow-sm border-b">
				<div class="flex items-center gap-2 mb-2">
					<a href="/projects" class="text-sm text-primary hover:underline">← All Projects</a>
				</div>
				<h1 class="text-2xl font-bold">{project.name}</h1>
				<p class="text-sm text-gray-600">{project.description ?? ''}</p>
				<div class="mt-2 flex gap-4 text-xs">
					<span
						><strong>Start:</strong> {new Date(project.start_date).toLocaleDateString()}</span
					>
					<span><strong>End:</strong> {new Date(project.end_date).toLocaleDateString()}</span>
					<span
						><strong>Status:</strong>
						<span
							class="rounded-full px-2 py-0.5 font-semibold {project.status === 'OPEN'
								? 'bg-green-100 text-green-800'
								: 'bg-red-100 text-red-800'}"
						>
							{project.status}
						</span>
					</span>
				</div>
			</div>
		{/if}

		<div class="flex-grow overflow-hidden">
			{#if $serviceRequestsRes.type === 'success'}
				<TableWithDetailPane {detailPaneOpen}>
					<div slot="detail-pane">
						<slot />
					</div>
					<div slot="table" class="relative flex h-full flex-col text-center">
						<Card bordered={true} class="mx-2 mb-1 mt-2 flex-1 overflow-hidden">
							<Card.Content slot="content" class="h-full p-0 sm:p-0">
								<div class="issues-table-override h-full overflow-y-auto">
									<Table class="h-full overflow-hidden rounded-md" {columns}>
										<Table.Header slot="header" />
										<Table.Body slot="body">
											{#each $serviceRequestsRes.value.serviceRequests.filter(r => r.project_id === Number($page.params.project_id)) as item}
												<Table.Body.Row
													tabIndex="0"
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

													<Table.Body.Row.Cell column={7}>
														<div class="flex w-full items-center justify-center">
															{#if item.removal_suggestions_count && item.removal_suggestions_count > 0}
																<span
																	class="inline-flex items-center justify-center rounded-full bg-red-600 px-2 pb-1 text-xs font-bold leading-none text-red-100"
																>
																	{item.removal_suggestions_count}
																</span>
															{:else}
																-
															{/if}
														</div>
													</Table.Body.Row.Cell>
												</Table.Body.Row>
											{/each}
										</Table.Body>

										<Table.Footer slot="footer">
											<div class="m-0 flex justify-end">
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
				<div class="p-4 text-red-600">Failed to load requests.</div>
			{:else}
				<div class="p-4">Loading requests...</div>
			{/if}
		</div>
	</div>
</AuthGuard>

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
