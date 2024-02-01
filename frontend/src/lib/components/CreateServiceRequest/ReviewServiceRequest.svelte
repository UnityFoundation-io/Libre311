<script lang="ts">
	import messages from '$media/messages.json';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import DisplayMultiSelectAttribute from '../DisplayMultiOrSingleSelectAttribute.svelte';
	import DisplayStringSelectAttribute from '../DisplayStringOrNumberSelectAttribute.svelte';
	import DisplayDateTimeSelectAttribute from '../DisplayDateTimeSelectAttribute.svelte';
	import { Badge } from 'stwui';
	import type {
		DateTimeServiceDefinitionAttributeInput,
		MultiSelectServiceDefinitionAttributeInput,
		NumberServiceDefinitionInput,
		SingleValueListServiceDefinitionAttributeInput,
		StringServiceDefinitionInput
	} from './ServiceDefinitionAttributes/shared';

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

	function getMultiOrSingleValueServiceAttributes(params: any) {
		const serviceAttributes:
			| MultiSelectServiceDefinitionAttributeInput[]
			| SingleValueListServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (
				entry.attribute.datatype == 'multivaluelist' ||
				entry.attribute.datatype == 'singlevaluelist'
			)
				serviceAttributes.push(entry);
		}
		return serviceAttributes;
	}

	function getStringOrNumberValueServiceAttributes(params: any) {
		const serviceAttributes: StringServiceDefinitionInput[] | NumberServiceDefinitionInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'string' || entry.attribute.datatype == 'number')
				serviceAttributes.push(entry);
		}
		return serviceAttributes;
	}

	function getDateTimeValueServiceAttributes(params: any) {
		const serviceAttributes: DateTimeServiceDefinitionAttributeInput[] = [];

		for (const [key, entry] of params.attributeMap.entries()) {
			if (entry.attribute.datatype == 'datetime') serviceAttributes.push(entry);
		}
		return serviceAttributes;
	}

	$: name = createName(params);
	$: multiOrSingleValueServiceAttributes = getMultiOrSingleValueServiceAttributes(params);
	$: stringOrNumberValueServiceAttributes = getStringOrNumberValueServiceAttributes(params);
	$: dateTimeValueServiceAttributes = getDateTimeValueServiceAttributes(params);
</script>

<div class="flex h-full items-center justify-center">
	<div class="mx-4 flex h-full w-full flex-col">
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

				{#if multiOrSingleValueServiceAttributes}
					{#each multiOrSingleValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayMultiSelectAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if stringOrNumberValueServiceAttributes}
					{#each stringOrNumberValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayStringSelectAttribute {attributes} />
						</div>
					{/each}
				{/if}

				{#if dateTimeValueServiceAttributes}
					{#each dateTimeValueServiceAttributes as attributes}
						<div class="mb-2">
							<DisplayDateTimeSelectAttribute {attributes} />
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

		<div class="mb-4">
			<div class="flex items-center justify-between">
				<button class="my-2 text-sm" type="button" on:click|preventDefault={() => {}}>
					{messages['reviewServiceRequest']['button_edit']}
				</button>
				<button class="submit my-2 text-sm" type="button" on:click|preventDefault={() => {}}>
					{messages['reviewServiceRequest']['button_submit']}
				</button>
			</div>
		</div>
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
