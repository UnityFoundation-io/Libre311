<script lang="ts">
	import messages from '$media/messages.json';
	import { slide } from 'svelte/transition';
	import { Button, Input } from 'stwui';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';
	import { createEventDispatcher } from 'svelte';
	import type { AttributeValue } from '$lib/services/Libre311/Libre311';
	import type { FormInputValue } from '$lib/utils/validation';

	interface EditAttributeInput {
		attribute_code: number;
		required: boolean;
		description: FormInputValue<string>;
		dataTypeDescription: FormInputValue<string>;
		values: AttributeValue[] | undefined;
	}

	const dispatch = createEventDispatcher<{ submit: EditAttributeInput }>();
	
	export let attribute: EditAttributeInput;

	let multivalueErrorMessage: string | undefined;

	$: multivalueErrorIndex = -1;

	function addEditValue() {
		if (attribute.values == undefined) {
			return;
		}
		const newId = attribute.values?.length
			? Number(attribute.values[attribute.values.length - 1].key) + 1
			: 1;
		attribute.values = [...attribute.values, { key: newId.toString(), name: '' }];
		multivalueErrorMessage = undefined;
	}

	function removeEditValue(index: number) {
		if (attribute.values) {
			for (let i = 0; i < attribute.values.length; i++) {
				if (i == index) {
					attribute.values = attribute.values.filter((_, i) => i !== index);
				}
			}
		}
	}

	function handleSubmit() {
		if (attribute.values) {
			for (let i = 0; i < attribute.values.length; i++) {
				if (attribute.values[i].name == '') {
					multivalueErrorMessage = 'You might want to add a value!';
					multivalueErrorIndex = i;
					return;
				} else {
					multivalueErrorMessage = undefined;
				}
			}
			dispatch('submit', attribute);
		}
	}
</script>

{#if attribute.values}
	<div class="flex flex-col" transition:slide|local={{ duration: 500 }}>
		<strong class="text-base">{'Values'}</strong>

		<ul>
			{#each attribute.values as value, index}
				<li class="my-2 flex justify-between" transition:slide|local={{ duration: 500 }}>
					{#if index == multivalueErrorIndex}
						<Input
							class="w-11/12 rounded-md"
							type="text"
							placeholder={messages['serviceDefinitionEditor']['attributes']['value_placeholder']}
							error={multivalueErrorMessage}
							bind:value={value.name}
						/>
					{:else}
						<Input
							class="w-11/12 rounded-md"
							type="text"
							placeholder={messages['serviceDefinitionEditor']['attributes']['value_placeholder']}
							bind:value={value.name}
						/>
					{/if}

					{#if index != 0}
						<Button on:click={() => removeEditValue(index)}>
							<XMark />
						</Button>
					{/if}
				</li>
			{/each}
		</ul>

		<Button class="mt-1" type="ghost" on:click={addEditValue}>
			{'+ Add'}
		</Button>

		<div class="my-2 flex items-center justify-between">
			<Button
				class="mr-1 w-1/2"
				aria-label="Close"
				type="ghost"
				on:click={() => window.history.back()}
			>
				{'Cancel'}
			</Button>

			<Button class="ml-1 w-1/2" aria-label="Submit" type="primary" on:click={handleSubmit}>
				{'Submit'}
			</Button>
		</div>
	</div>
{/if}
