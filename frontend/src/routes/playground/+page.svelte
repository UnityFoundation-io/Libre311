<script lang="ts">
	import SideBarMainContentLayout from '$lib/components/SideBarMainContentLayout.svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		GetServiceListResponse,
		ServiceCode,
		ServiceDefinition
	} from '$lib/services/Libre311/Libre311';

	import { ASYNC_IN_PROGRESS, asAsyncSuccess, type AsyncResult } from '$lib/services/http';

	import { onMount } from 'svelte';
	import ServiceRequestDetailsForm from '$lib/components/CreateServiceRequest/ServiceRequestDetailsForm.svelte';

	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServiceCode: ServiceCode | undefined;
	let serviceDefinition: AsyncResult<ServiceDefinition> | undefined;

	$: if (selectedServiceCode) getServiceDefinition(selectedServiceCode);

	async function getServiceDefinition(service_code: ServiceCode) {
		const res = await libre311.getServiceDefinition({ service_code });
		serviceDefinition = asAsyncSuccess(res);
	}

	onMount(async () => {
		const res = await libre311.getServiceList();
		serviceList = asAsyncSuccess(res);
	});

	$: console.log(serviceList);
	$: console.log(serviceDefinition);
</script>

<SideBarMainContentLayout>
	<ServiceRequestDetailsForm slot="side-bar" />
	<div slot="main-content">map goes here</div>
</SideBarMainContentLayout>
