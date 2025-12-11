<script lang="ts">
	import { run } from 'svelte/legacy';

	import messages from '$media/messages.json';
	import ServiceRequestPreview from '$lib/components/ServiceRequestPreview.svelte';

	import Pagination from '$lib/components/Pagination.svelte';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import { page } from '$app/stores';
	import {
		useServiceRequestsContext,
		useServiceRequestsResponseStore
	} from '$lib/context/ServiceRequestsContext';

	const ctx = useServiceRequestsContext();
	const serviceRequestsRes = ctx.serviceRequestsResponse;

	const linkResolver = useLibre311Context().linkResolver;

	const serviceRequestResStore = useServiceRequestsResponseStore();
	let listElement: HTMLElement | undefined = $state();

	run(() => {
		if ($serviceRequestResStore.type === 'success') {
			listElement?.scrollIntoView();
			window.scrollTo({ top: 0, behavior: 'instant' });
		}
	});
</script>

{#if $serviceRequestsRes.type === 'success'}
	<div bind:this={listElement}>
		<div class="sticky top-0 border-b-2 bg-white">
			<div class="flex items-center justify-between">
				<div>
					<h3 class="ml-4 text-base">{messages['sidebar']['title']}</h3>
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
		</div>

		<ul>
			{#each $serviceRequestsRes.value.serviceRequests as serviceRequest}
				<li class="m-3">
					<ServiceRequestPreview
						{serviceRequest}
						detailsLink={linkResolver.issueDetailsTable(
							$page.url,
							serviceRequest.service_request_id
						)}
					/>
				</li>
			{/each}
		</ul>
	</div>
{/if}
