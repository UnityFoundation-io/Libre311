<script lang="ts">
	import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
	import { statusToColorMap, statusToTextMap } from '$lib/utils/functions';
	import { Badge } from 'stwui';

	let clazz: string = '';
	export { clazz as class };
	export let status: ServiceRequestStatus;
</script>

<div
	class="service-request-status-badge-wrapper"
	style="--status-background-color: {statusToColorMap[status]}"
>
	<Badge class={clazz}>{statusToTextMap[status]}</Badge>
</div>

<style>
	.service-request-status-badge-wrapper {
		display: contents;
	}

	.service-request-status-badge-wrapper :global(.stwui-badge) {
		background-color: var(--status-background-color);
	}
</style>
