<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import { createServiceRequestsContext } from './ServiceRequestsContext';

	const libreService = useLibre311Service();
	const { offlineQueue, syncSignal } = useLibre311Context();
	const serviceRequestsContext = createServiceRequestsContext(libreService, page, offlineQueue, syncSignal);
</script>

<slot {serviceRequestsContext} />
