<script lang="ts">
	import messages from '$media/messages.json';
	import { slide } from 'svelte/transition';
	import type { AttributeEditValue } from '../types';
	import { Button, Input } from 'stwui';
	import XMark from '$lib/components/Svg/outline/XMark.svelte';

	export let values: AttributeEditValue[];
	export let multivalueErrorIndex: number;
	export let multivalueErrorMessage: string | undefined;

	function addEditValue() {
		if (values == undefined) {
			return;
		}
		const newId = values?.length ? Number(values[values.length - 1].key) + 1 : 1;
		values = [...values, { key: newId.toString(), name: '' }];
		multivalueErrorMessage = undefined;
	}

	function removeEditValue(index: number) {
		if (values) {
			for (let i = 0; i < values.length; i++) {
				if (i == index) {
					values = values.filter((_, i) => i !== index);
				}
			}
		}
	}
</script>

<div class="flex flex-col" transition:slide|local={{ duration: 500 }}>
	<strong class="text-base">{'Values'}</strong>

	<ul>
		{#each values as value, index}
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
</div>
