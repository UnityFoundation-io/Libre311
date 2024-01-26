<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		GetServiceListResponse,
		ServiceCode,
		ServiceDefinition
	} from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { Select, TextArea } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import { onMount } from 'svelte';
	import MultiSelectServiceDefinitionAttribute from './ServiceDefinitionAttributes/MultiSelectServiceDefinitionAttribute.svelte';
	import DateTimeServiceDefinitionAttribute from './ServiceDefinitionAttributes/DateTimeServiceDefinitionAttribute.svelte';
	import StringServiceDefinitionAttribute from './ServiceDefinitionAttributes/StringServiceDefinitionAttribute.svelte';
	import SingleValueListServiceDefinitionAttribute from './ServiceDefinitionAttributes/SingleValueListServiceDefinitionAttribute.svelte';
	import NumberServiceDefinitionAttribute from './ServiceDefinitionAttributes/NumberServiceDefinitionAttribute.svelte';
	import TextServiceDefinitionAttribute from './ServiceDefinitionAttributes/TextServiceDefinitionAttribute.svelte';
	// export let params: Readyonly<Partial<CreateServiceRequestParams>> = {};
	//
	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let serviceDefinition: AsyncResult<ServiceDefinition> | undefined;
	let selectedServiceCode: ServiceCode | undefined;

	onMount(() => {
		libre311
			.getServiceList()
			.then((res) => (serviceList = asAsyncSuccess(res)))
			.catch((err) => (serviceList = asAsyncFailure(err)));
	});

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	function issueTypeChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedServiceCode = target.value;
	}

	// 1 getServiceList
	// 2 render the servicedefintionattribute form inputs
	// 3
</script>

{#if serviceList.type === 'success'}
	{@const selectOptions = createSelectOptions(serviceList.value)}
	<Select
		name="select-1"
		placeholder="Issue Type"
		on:change={issueTypeChange}
		options={selectOptions}
		class="relative mx-8 my-4"
	>
		<Select.Options slot="options">
			{#each selectOptions as option}
				<Select.Options.Option {option} />
			{/each}
		</Select.Options>
	</Select>
{/if}

{#if serviceDefinition?.type == 'success'}
	{#if serviceDefinition?.type === 'success'}
		{#each serviceDefinition.value.attributes as attribute}
			{#if attribute.datatype === 'multivaluelist'}
				<MultiSelectServiceDefinitionAttribute {attribute} on:change={(e) => console.log(e)} />
			{:else if attribute.datatype === 'datetime'}
				<DateTimeServiceDefinitionAttribute {attribute} />
			{:else if attribute.datatype == 'string'}
				<StringServiceDefinitionAttribute {attribute} />
			{:else if attribute.datatype == 'singlevaluelist'}
				<SingleValueListServiceDefinitionAttribute {attribute} />
			{:else if attribute.datatype == 'number'}
				<NumberServiceDefinitionAttribute {attribute} />
			{:else if attribute.datatype == 'text'}
				<TextServiceDefinitionAttribute {attribute} />
			{/if}
		{/each}
		<TextArea name="comments" placeholder="Description" class="relative mx-8 my-4" />
	{/if}
{/if}
