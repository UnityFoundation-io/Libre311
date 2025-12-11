<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { createServiceRequestsContext } from './ServiceRequestsContext';
	interface Props {
		children?: import('svelte').Snippet<[any]>;
	}

	let { children }: Props = $props();

	const libreService = useLibre311Service();
	const serviceRequestsContext = createServiceRequestsContext(libreService, page);
</script>

{@render children?.({ serviceRequestsContext, })}
