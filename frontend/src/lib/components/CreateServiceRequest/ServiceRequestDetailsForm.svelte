<script lang="ts" context="module"></script>

<script lang="ts">
	import { onMount } from 'svelte';
	import { useLibre311Service } from '$lib/context/Libre311Context';
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
		type AttributeInputMap
	} from './ServiceDefinitionAttributes/shared';
	import { createEventDispatcher, type ComponentEvents } from 'svelte';
	import type { CreateServiceRequestUIParams, StepChangeEvent } from './shared';
	import StepControls from './StepControls.svelte';
	import type { Service } from '$lib/services/Libre311/Libre311';

	export let params: Partial<CreateServiceRequestUIParams>;

	const libre311 = useLibre311Service();
	const dispatch = createEventDispatcher<StepChangeEvent>();

	let asyncAttributeInputMap: AsyncResult<AttributeInputMap> | undefined;
	let selectedService: Service | undefined = params.service;
	let imageData: string | undefined;

	$: updateAttributeMap(selectedService);

	function updateAttributeMap(service: Service | undefined) {
		if (!service) {
			asyncAttributeInputMap = undefined;
			return;
		}
		getServiceDefinition(service);
	}

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
		if (!asyncAttributeInputMap || asyncAttributeInputMap.type !== 'success') return;

		let hasError = false;
		for (const input of asyncAttributeInputMap.value.values()) {
			if (input.attribute.required) {
				switch (input.datatype) {
					case 'multivaluelist':
					case 'string':
					case 'text':
						if (input.value && input.value.length > 0) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
						break;
					case 'datetime':
					case 'singlevaluelist':
					case 'number':
						if (input.value) input.error = '';
						else {
							hasError = true;
							input.error = errMsg;
						}
						break;
					default:
						throw Error('Illegal argument, attribute datatype not supported');
				}
			} else {
				input.error = undefined;
			}
		}

		// if no errors, dispatch
		if (!hasError) {
			if (!selectedService) throw Error('Service must be selected');
			const updatedParams = {
				attributeMap: asyncAttributeInputMap.value,
				service: selectedService
			};
			dispatch('stepChange', updatedParams);
		} else {
			asyncAttributeInputMap = asAsyncSuccess(asyncAttributeInputMap.value);
		}
	}

	function handleServiceSelected(e: ComponentEvents<SelectARequestCategory>['serviceSelected']) {
		selectedService = e.detail;
	}

	onMount(() => {
		if (params.file) {
			let reader = new FileReader();
			reader.readAsDataURL(params.file);

			reader.onloadend = function () {
				const result: String = new String(reader.result);
				imageData = result.toString();
			};
		}
	});
</script>

<form class="flex-container">
	<div>
		{#if imageData}
			<div class="image-container relative mx-auto my-4">
				<img class="rounded-lg" src={imageData} alt="preview" />
			</div>
		{/if}
		<SelectARequestCategory {params} on:serviceSelected={handleServiceSelected} />
		{#if asyncAttributeInputMap?.type == 'success'}
			<div>
				{#each asyncAttributeInputMap.value.values() as input}
					{#if input.datatype == 'multivaluelist'}
						<MultiSelectServiceDefinitionAttribute {input} />
					{:else if input.datatype == 'datetime'}
						<DateTimeServiceDefinitionAttribute {input} />
					{:else if input.datatype == 'string'}
						<StringServiceDefinitionAttribute {input} />
					{:else if input.datatype == 'singlevaluelist'}
						<SingleValueListServiceDefinitionAttribute {input} />
					{:else if input.datatype == 'number'}
						<NumberServiceDefinitionAttribute {input} />
					{:else if input.datatype == 'text'}
						<TextServiceDefinitionAttribute {input} />
					{/if}
				{/each}
				<TextArea
					bind:value={params.description}
					name="comments"
					placeholder="Description"
					class="relative  my-4"
				/>
			</div>
		{/if}
	</div>

	<StepControls on:click={validate}>
		<svelte:fragment slot="submit-text">Confirm Details</svelte:fragment>
	</StepControls>
</form>

<style>
	.flex-container {
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		height: 100%;
	}
	.image-container {
		display: flex;
		justify-content: center;
	}
	img {
		max-height: 15rem;
	}
	/* Desktop */
	@media only screen and (min-width: 769px) {
		img {
			max-height: 20rem;
		}
	}
</style>
