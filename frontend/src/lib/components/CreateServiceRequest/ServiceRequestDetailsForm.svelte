<script lang="ts" context="module"></script>

<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { CreateServiceRequestParams, Service } from '$lib/services/Libre311/Libre311';
	import { asAsyncSuccess, type AsyncResult } from '$lib/services/http';
	import { TextArea } from 'stwui';

	import MultiSelectServiceDefinitionAttribute from './ServiceDefinitionAttributes/MultiSelectServiceDefinitionAttribute.svelte';
	import DateTimeServiceDefinitionAttribute from './ServiceDefinitionAttributes/DateTimeServiceDefinitionAttribute.svelte';
	import StringServiceDefinitionAttribute from './ServiceDefinitionAttributes/StringServiceDefinitionAttribute.svelte';
	import SingleValueListServiceDefinitionAttribute from './ServiceDefinitionAttributes/SingleValueListServiceDefinitionAttribute.svelte';
	import NumberServiceDefinitionAttribute from './ServiceDefinitionAttributes/NumberServiceDefinitionAttribute.svelte';
	import TextServiceDefinitionAttribute from './ServiceDefinitionAttributes/TextServiceDefinitionAttribute.svelte';

	import SelectARequestCategory from './SelectARequestCategory.svelte';
	import {
		createAttributeInputMap,
		type AttributeInputMap,
		isMultiSelectServiceDefinitionAttributeInput,
		isSingleValueListServiceDefinitionAttributeInput,
		isDateTimeServiceDefinitionAttributeInput,
		isStringServiceDefinitionInput,
		isNumberServiceDefinitionInput,
		isTextServiceDefinitionAttributeInput
	} from './ServiceDefinitionAttributes/shared';
	import { createEventDispatcher, type ComponentEvents } from 'svelte';
	import type { StepChangeEvent } from './types';

	export let params: Partial<CreateServiceRequestParams>;

	const libre311 = useLibre311Service();
	const dispatch = createEventDispatcher<StepChangeEvent>();

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> | undefined;
	let selectedService: Service | undefined = params.service;

	$: if (selectedService) getServiceDefinition(selectedService);

	async function getServiceDefinition(selectedService: Service) {
		asyncAttributeInputMap = undefined;
		if (selectedService.metadata) {
			const payload = { service_code: selectedService.service_code };
			const res = await libre311.getServiceDefinition(payload);
			asyncAttributeInputMap = asAsyncSuccess(createAttributeInputMap(res, params));
		} else {
			asyncAttributeInputMap = asAsyncSuccess(new Map());
		}
	}

	function validate() {
		const errMsg = 'This value is required';

		if (asyncAttributeInputMap?.type === 'success') {
			let hasError = false;
			for (const input of asyncAttributeInputMap.value.values()) {
				if (input.attribute.required) {
					if (isMultiSelectServiceDefinitionAttributeInput(input)) {
						if (input.value && input.value.length > 0) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else if (isDateTimeServiceDefinitionAttributeInput(input)) {
						if (input.value) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else if (isStringServiceDefinitionInput(input)) {
						if (input.value && input.value.length > 0) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else if (isSingleValueListServiceDefinitionAttributeInput(input)) {
						if (input.value) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else if (isNumberServiceDefinitionInput(input)) {
						if (input.value) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else if (isTextServiceDefinitionAttributeInput(input)) {
						if (input.value && input.value.length > 0) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
					} else {
						throw Error('Illegal argument, attribute datatype not supported');
					}
				} else {
					input.error = undefined;
				}
			}
			// if no errors, dispatch
			if (!hasError) {
				if (!selectedService) throw Error('Service must be selected');
				const updatedParams: Partial<CreateServiceRequestParams> = {
					attributeMap: asyncAttributeInputMap.value,
					service: selectedService
				};
				dispatch('stepChange', updatedParams);
			} else {
				asyncAttributeInputMap = asAsyncSuccess(asyncAttributeInputMap.value);
			}
		}
	}

	function handleServiceSelected(e: ComponentEvents<SelectARequestCategory>['serviceSelected']) {
		selectedService = e.detail;
	}
</script>

<form>
	<SelectARequestCategory {params} on:serviceSelected={handleServiceSelected} />
	{#if asyncAttributeInputMap?.type == 'success'}
		{#each asyncAttributeInputMap.value.values() as input}
			{#if isMultiSelectServiceDefinitionAttributeInput(input)}
				<MultiSelectServiceDefinitionAttribute {input} />
			{:else if isDateTimeServiceDefinitionAttributeInput(input)}
				<DateTimeServiceDefinitionAttribute {input} />
			{:else if isStringServiceDefinitionInput(input)}
				<StringServiceDefinitionAttribute {input} />
			{:else if isSingleValueListServiceDefinitionAttributeInput(input)}
				<SingleValueListServiceDefinitionAttribute {input} />
			{:else if isNumberServiceDefinitionInput(input)}
				<NumberServiceDefinitionAttribute {input} />
			{:else if isTextServiceDefinitionAttributeInput(input)}
				<TextServiceDefinitionAttribute {input} />
			{/if}
		{/each}
		<TextArea name="comments" placeholder="Description" class="relative mx-8 my-4" />

		<button type="submit" on:click|preventDefault={validate}>Submit</button>
	{/if}
</form>
