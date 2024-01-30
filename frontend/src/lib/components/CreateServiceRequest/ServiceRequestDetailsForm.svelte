<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type {
		AttributeResponse,
		GetServiceListResponse,
		ServiceCode,
		ServiceDefinition,
		ServiceDefinitionAttribute
	} from '$lib/services/Libre311/Libre311';
	import {
		ASYNC_IN_PROGRESS,
		asAsyncSuccess,
		type AsyncResult,
		asAsyncFailure
	} from '$lib/services/http';
	import { Select, TextArea } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import { onMount, type ComponentEvents } from 'svelte';
	import MultiSelectServiceDefinitionAttribute from './ServiceDefinitionAttributes/MultiSelectServiceDefinitionAttribute.svelte';
	import DateTimeServiceDefinitionAttribute from './ServiceDefinitionAttributes/DateTimeServiceDefinitionAttribute.svelte';
	import StringServiceDefinitionAttribute from './ServiceDefinitionAttributes/StringServiceDefinitionAttribute.svelte';
	import SingleValueListServiceDefinitionAttribute from './ServiceDefinitionAttributes/SingleValueListServiceDefinitionAttribute.svelte';
	import NumberServiceDefinitionAttribute from './ServiceDefinitionAttributes/NumberServiceDefinitionAttribute.svelte';
	import TextServiceDefinitionAttribute from './ServiceDefinitionAttributes/TextServiceDefinitionAttribute.svelte';
	import type { AttributeSelection } from './ServiceDefinitionAttributes/shared';
	import { map } from 'leaflet';

	// export let params: Readyonly<Partial<CreateServiceRequestParams>> = {};
	//
	const libre311 = useLibre311Service();

	let serviceList: AsyncResult<GetServiceListResponse> = ASYNC_IN_PROGRESS;
	let selectedServiceDefinition: AsyncResult<ServiceDefinition> | undefined;
	let selectedServiceCode: ServiceCode | undefined;

	let attributeResponseMap: Map<
		ServiceDefinitionAttribute['code'],
		AttributeResponse[] | AttributeResponse
	> = new Map();

	$: attributeResponses = deriveResponses(attributeResponseMap);
	$: console.log(attributeResponses);
	$: if (selectedServiceCode) getServiceDefinition(selectedServiceCode);

	function deriveResponses(
		attributeResponseMap: Map<
			ServiceDefinitionAttribute['code'],
			AttributeResponse[] | AttributeResponse
		>
	): AttributeResponse[] {
		const attributeResponseArr: AttributeResponse[] = [];

		for (const attributeRes of attributeResponseMap.values()) {
			if (Array.isArray(attributeRes)) {
				attributeResponseArr.push(...attributeRes);
			} else attributeResponseArr.push(attributeRes);
		}
		return attributeResponseArr;
	}

	onMount(() => {
		libre311
			.getServiceList()
			.then((res) => (serviceList = asAsyncSuccess(res)))
			.catch((err) => (serviceList = asAsyncFailure(err)));
	});

	function updateAttributeResponseMap(e: CustomEvent<AttributeSelection>) {
		attributeResponseMap.set(e.detail.code, e.detail.attributeResponse);
		attributeResponseMap = attributeResponseMap;
	}

	function createSelectOptions(res: GetServiceListResponse): SelectOption[] {
		return res.map((s) => ({ value: s.service_code, label: s.service_name }));
	}

	function issueTypeChange(e: Event) {
		const target = e.target as HTMLSelectElement;
		selectedServiceCode = target.value;
	}

	async function getServiceDefinition(service_code: ServiceCode) {
		attributeResponseMap = new Map();
		selectedServiceDefinition = undefined;
		if (
			serviceList.type === 'success' &&
			serviceList.value.find((s) => s.service_code == service_code)?.metadata
		) {
			const res = await libre311.getServiceDefinition({ service_code });
			selectedServiceDefinition = asAsyncSuccess(res);
		}
	}

	// 1 getServiceList 									(x)
	// 2 render the servicedefintionattribute form inputs 	(x)
	// 3 capture what the user inputs in those inputs 		(x)
	// 4 validate the inputs
	// 5 dispatch the event given the inputs are valid
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

{#if selectedServiceDefinition?.type == 'success'}
	{#if selectedServiceDefinition?.type === 'success'}
		{#each selectedServiceDefinition.value.attributes as attribute}
			{#if attribute.datatype === 'multivaluelist'}
				<MultiSelectServiceDefinitionAttribute on:change={updateAttributeResponseMap} {attribute} />
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
	{/if}
{/if}
<TextArea name="comments" placeholder="Description" class="relative mx-8 my-4" />
