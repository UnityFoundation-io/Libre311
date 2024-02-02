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

	$: name = createName(params);
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

				{#each params.attributeMap.values() as attributes}
					<div class="mb-2">
						{#if attributes.datatype == 'multivaluelist'}
							<DisplayMultiAttribute {attributes} />
						{:else if attributes.datatype == 'datetime'}
							<DisplayDateTimeAttribute {attributes} />
						{:else if attributes.datatype == 'string'}
							<DisplayStringAttribute {attributes} />
						{:else if attributes.datatype == 'singlevaluelist'}
							<DisplaySingleAttribute {attributes} />
						{:else if attributes.datatype == 'number'}
							<DisplayNumberAttribute {attributes} />
						{:else if attributes.datatype == 'text'}
							<DisplayTextAttribute {attributes} />
						{/if}
					</div>
				{/each}

				{#if params.description ?? ''}
					<div class="mb-1">
						<strong class="text-base">{messages['serviceRequest']['description']}</strong>
						<p class="text-sm">{params.description}</p>
					</div>
				{/if}

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
</style>
