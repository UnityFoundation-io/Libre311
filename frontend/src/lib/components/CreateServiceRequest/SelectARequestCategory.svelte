<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		GetServiceListResponse,
		Service,
		ServiceCode
	} from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { Button, Progress, Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import { createEventDispatcher, onMount } from 'svelte';
	import { arrowPath } from '../Svg/outline/arrowPath';
	import type { CreateServiceRequestUIParams } from './shared';
	import messages from '$media/messages.json';
	import { setUpAlertRole } from '$lib/utils/functions';

	export let params: Partial<CreateServiceRequestUIParams>;

	const libre311 = useLibre311Service();
	const dispatch = createEventDispatcher<{ serviceSelected: Service | undefined }>();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServiceCode: ServiceCode | undefined = params?.service?.service_code;
	let selectedService: Service | undefined;

	$: if (selectedServiceCode && serviceList.type === 'success') {
		selectedService = findService(selectedServiceCode);
		dispatch('serviceSelected', selectedService);
	}

	onMount(fetchServiceList);

	function fetchServiceList() {
		libre311
			.getServiceList()
			.then((res) => {
				serviceList = asAsyncSuccess(res);
			})
			.catch((err) => (serviceList = asAsyncFailure(err)));
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	function findService(serviceCode: ServiceCode) {
		if (serviceList.type == 'success') {
			return (selectedService = serviceList.value.find((s) => s.service_code == serviceCode));
		}
		throw Error('Service list not loaded');
	}

	function issueTypeChange(e: Event) {
		selectError = '';
		const target = e.target as HTMLSelectElement;
		selectedServiceCode = Number(target.value);
		if (serviceList.type == 'success') {
			const selectedService = findService(selectedServiceCode);
			dispatch('serviceSelected', selectedService);
		}
	}

	let selectRoot: HTMLElement;

	export let selectError = '';

	$: setUpAlertRole(
		selectError,
		selectRoot,
		'input#select-request-type',
		'select-request-type-error'
	);
</script>

{#if serviceList.type === 'success'}
	{@const selectOptions = createSelectOptions(serviceList.value)}
	<div bind:this={selectRoot}>
		<Select
			error={selectError}
			name="select-request-type"
			placeholder={messages['serviceRequest']['request_type']}
			on:change={issueTypeChange}
			options={selectOptions}
			class="relative my-4"
		>
			<Select.Label slot="label">
				<strong class="text-base">{messages['serviceRequest']['request_type']}:</strong>
			</Select.Label>

			<Select.Options slot="options">
				{#each selectOptions as option}
					<Select.Options.Option {option} />
				{/each}
			</Select.Options>
		</Select>
	</div>
{:else if serviceList.type === 'inProgress'}
	<Select
		disabled
		name="select-request-type"
		placeholder="Loading Request Types..."
		on:change={issueTypeChange}
		options={[]}
		class="relative mx-8 my-4"
	></Select>
	<div class="mx-8 my-4">
		<Progress value={0} indeterminate />
	</div>
{:else}
	<Select
		disabled
		name="select-request-type"
		placeholder="Failed to Load Request Types"
		on:change={issueTypeChange}
		options={[]}
		class="relative mx-8 my-4"
	></Select>
	<div class="flex content-center justify-center">
		<Button on:click={() => fetchServiceList()} type="primary">
			<Button.Leading data={arrowPath} slot="leading" />
			Reload
		</Button>
	</div>
{/if}
