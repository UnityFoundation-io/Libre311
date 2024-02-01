<script lang="ts">
	import messages from '$media/messages.json';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import DisplayMultiAttribute from './DisplayServiceDefinitionAttributes/DisplayMultiAttribute.svelte';
	import DisplaySingleAttribute from './DisplayServiceDefinitionAttributes/DisplaySingleAttribute.svelte';
	import DisplayStringAttribute from './DisplayServiceDefinitionAttributes/DisplayStringAttribute.svelte';
	import DisplayNumberAttribute from './DisplayServiceDefinitionAttributes/DisplayNumberAttribute.svelte';
	import DisplayDateTimeAttribute from './DisplayServiceDefinitionAttributes/DisplayDateTimeAttribute.svelte';
	import DisplayTextAttribute from './DisplayServiceDefinitionAttributes/DisplayTextAttribute.svelte';
	import { Badge } from 'stwui';
	import type {
		DateTimeServiceDefinitionAttributeInput,
		MultiSelectServiceDefinitionAttributeInput,
		NumberServiceDefinitionInput,
		SingleValueListServiceDefinitionAttributeInput,
		StringServiceDefinitionInput,
		TextServiceDefinitionAttributeInput
	} from './ServiceDefinitionAttributes/shared';
	import StepControls from './StepControls.svelte';

	export let params: CreateServiceRequestParams;

	function createName(params: CreateServiceRequestParams) {
		if (params.first_name || params.last_name)
			return `${params.first_name ?? ''} ${params.last_name ?? ''}`;
	}

	function getTimeStamp(): string | null | undefined {
		let timeStamp = new Date().getTime();
		return timeStamp
			? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
			: '';
	}

	function getMultiValueServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: MultiSelectServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'multivaluelist')
				serviceAttributes.push(entry as MultiSelectServiceDefinitionAttributeInput);
		}
		return serviceAttributes;
	}

	function getSingleValueServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: SingleValueListServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'singlevaluelist')
				serviceAttributes.push(entry as SingleValueListServiceDefinitionAttributeInput);
		}
		return serviceAttributes;
	}

	function getStringValueServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: StringServiceDefinitionInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'string') serviceAttributes.push(entry as any);
		}
		return serviceAttributes;
	}

	function getNumberValueServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: NumberServiceDefinitionInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'number') serviceAttributes.push(entry as any);
		}
		return serviceAttributes;
	}

	function getDateTimeValueServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: DateTimeServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'datetime')
				serviceAttributes.push(entry as DateTimeServiceDefinitionAttributeInput);
		}
		return serviceAttributes;
	}

	function getTextAreaServiceAttributes(params: CreateServiceRequestParams) {
		const serviceAttributes: TextServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'text')
				serviceAttributes.push(entry as TextServiceDefinitionAttributeInput);
		}
		return serviceAttributes;
	}

	$: name = createName(params);
	$: multiValueServiceAttributes = getMultiValueServiceAttributes(params);
	$: singleValueServiceAttributes = getSingleValueServiceAttributes(params);
	$: stringValueServiceAttributes = getStringValueServiceAttributes(params);
	$: numberValueServiceAttributes = getNumberValueServiceAttributes(params);
	$: dateTimeValueServiceAttributes = getDateTimeValueServiceAttributes(params);
	$: textValueServiceAttributes = getTextAreaServiceAttributes(params);
</script>

<div class="flex h-full items-center justify-center">
	<div class="flex h-full w-full flex-col">
		<div class="mt-4 flex-grow">
			<h1 class="text-lg">{messages['reviewServiceRequest']['title']}</h1>

			<div class="my-2">
				<div class="flow-root">
					<Badge class="float-right text-sm" type="warn">Open</Badge>
				</div>

				<p class="my-1 text-sm font-extralight">{getTimeStamp()}</p>

				{#if params.media_url}
					<div class="serviceImage" style={`background-image: url('${params.media_url}');`} />
				{/if}

				<div class="serviceTitle mt-2 flow-root">
					<h1 class="float-left text-lg">{params.service.service_name}</h1>
				</div>

				<div class="mb-2">
					<p class="text-sm">{params.address_string}</p>
				</div>

				{#if multiValueServiceAttributes}
					{#each multiValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayMultiAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if singleValueServiceAttributes}
					{#each singleValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplaySingleAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if stringValueServiceAttributes}
					{#each stringValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayStringAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if numberValueServiceAttributes}
					{#each numberValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayNumberAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if dateTimeValueServiceAttributes}
					{#each dateTimeValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayDateTimeAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if textValueServiceAttributes}
					{#each textValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayTextAttribute {attributes} />
						</div>
					{/each}
				{/if}

				<div class="mb-1">
					<strong class="text-base">{messages['serviceRequest']['description']}</strong>
					<p class="text-sm">{params.service.description}</p>
				</div>

				{#if name}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['citizen_contact']}</strong>
						<p class="text-sm">{name}</p>
					</div>
				{/if}
			</div>
		</div>

		<StepControls on:click={() => alert('todo submit to server')}>
			<svelte:fragment slot="submit-text"
				>{messages['reviewServiceRequest']['button_submit']}</svelte:fragment
			>
		</StepControls>
	</div>
</div>

<style>
	.serviceTitle {
		color: hsl(var(--primary));
	}

	.serviceImage {
		height: 0;
		padding-top: 56.25%;
		overflow-x: hidden;
		overflow-y: hidden;
		background-position: center;
		background-size: cover;
		border-radius: 10px;
	}

	.submit {
		padding: 0.5rem 2rem;
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		border: 1px solid hsl(var(--primary));
		border-radius: 10px;
	}

	.submit:hover {
		--tw-surface-opacity: 0.1;
		border: 1px solid hsl(var(--primary));
		color: hsl(var(--content));
		background-color: hsl(var(--surface) / var(--tw-surface-opacity));
	}
</style>
