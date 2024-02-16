<script lang="ts">
	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';

	import Pagination from '$lib/components/Pagination.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import { useServiceRequestsContext } from '$lib/context/ServiceRequestsContext';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	const linkResolver = useLibre311Context().linkResolver;
</script>

{#if $serviceRequestsRes.type === 'success'}
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

	<ul>
		{#each $serviceRequestsRes.value.serviceRequests as serviceRequest}
			<li class="m-3">
				<ServiceRequestPreview
					{serviceRequest}
					detailsLink={`/issues/table/${serviceRequest.service_request_id}`}
				/>
			</li>
		{/each}
	</ul>
{/if}
