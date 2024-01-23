<script lang="ts">
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';
	import Pagination from '$lib/components/Pagination.svelte';
	import { page } from '$app/stores';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import MapListToggle from '$lib/components/MapListToggle.svelte';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	const linkResolver = useLibre311Context().linkResolver;
</script>

<div class="flex items-center justify-center">
	{#if $serviceRequestsRes.type === 'success'}
		<div class="w-full max-w-lg">
			<div class="flex justify-center my-4">
				<MapListToggle />
			</div>

			<div class="flex justify-center">
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

			<ul>
				{#each $serviceRequestsRes.value.serviceRequests as serviceRequest}
					<li class="m-3">
						<ServiceRequestPreview
							{serviceRequest}
							detailsLink={linkResolver.issueDetailsMobile(
								$page.url,
								serviceRequest.service_request_id
							)}
						/>
					</li>
				{/each}
			</ul>
		</div>
	{/if}
</div>
