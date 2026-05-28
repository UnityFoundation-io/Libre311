<script lang="ts">
	import { onMount } from 'svelte';
	import { TextArea } from 'stwui';
	import { createEventDispatcher } from 'svelte';
	import type { CreateServiceRequestUIParams, StepChangeEvent } from './shared';
	import StepControls from './StepControls.svelte';
	import type { Service, TextServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import type { AttributeInputMap } from './ServiceDefinitionAttributes/shared';
	import { useLibre311Service } from '$lib/context/Libre311Context';

	export let params: Partial<CreateServiceRequestUIParams>;
	export let service: Service;

	const libre311 = useLibre311Service();
	const dispatch = createEventDispatcher<StepChangeEvent>();

	let questionAttribute: TextServiceDefinitionAttribute | undefined;
	let answer: string = '';
	let answerError: string = '';
	let imageData: string | undefined;
	let loading = true;

	onMount(async () => {
		try {
			const def = await libre311.getServiceDefinition({ service_code: service.service_code });
			const attr = def.attributes.find((a) => a.datatype === 'text');
			if (attr) questionAttribute = attr as TextServiceDefinitionAttribute;
		} finally {
			loading = false;
		}

		if (params.file) {
			const reader = new FileReader();
			reader.readAsDataURL(params.file);
			reader.onloadend = () => {
				imageData = String(reader.result);
			};
		}
	});

	function validate() {
		answerError = '';
		if (!questionAttribute) return;
		if (questionAttribute.required && (!answer || answer.trim().length === 0)) {
			answerError = 'This value is required';
			return;
		}
		const attributeMap: AttributeInputMap = new Map();
		attributeMap.set(questionAttribute.code, {
			datatype: 'text',
			attribute: questionAttribute,
			value: answer
		});
		dispatch('stepChange', { service, attributeMap });
	}
</script>

<form class="flex-container">
	<div>
		{#if imageData}
			<div class="image-container relative mx-auto my-4">
				<img class="rounded-lg" src={imageData} alt="preview" />
			</div>
		{/if}

		{#if loading}
			<p class="my-4 text-sm text-gray-500">Loading...</p>
		{:else if questionAttribute}
			{#if service.description}
				<p class="my-3 whitespace-pre-wrap text-sm text-gray-600">{service.description}</p>
			{/if}

			<TextArea bind:value={answer} name="photo-voice-answer" placeholder="Your response..." class="relative my-4">
				<TextArea.Label slot="label">
					<strong class="text-base">{questionAttribute.description}</strong>
				</TextArea.Label>
			</TextArea>

			{#if answerError}
				<p class="text-sm text-error">{answerError}</p>
			{/if}
		{:else}
			<p class="my-4 text-sm text-error">
				This feature is not fully configured. Contact an administrator.
			</p>
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
	@media only screen and (min-width: 769px) {
		img {
			max-height: 20rem;
		}
	}
</style>
