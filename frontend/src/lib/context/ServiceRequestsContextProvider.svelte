<script lang="ts">
	import { page } from '$app/stores';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import { createServiceRequestsContext } from './ServiceRequestsContext';

	const libreService = useLibre311Service();
	const serviceRequestsContext = createServiceRequestsContext(libreService, page);
</script>

<slot {serviceRequestsContext} />
