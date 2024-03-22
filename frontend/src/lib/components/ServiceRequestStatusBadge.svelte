<script lang="ts">
	import type { ServiceRequestStatus } from '$lib/services/Libre311/Libre311';
	import { statusToColorMap, statusToTextMap } from '$lib/utils/functions';
	import { Badge } from 'stwui';

	let clazz: string = '';
	export { clazz as class };
	export let status: ServiceRequestStatus;
</script>

<Badge class={`${clazz} bg-[${statusToColorMap[status]}]`}>{statusToTextMap[status]}</Badge>
