<script lang="ts">
	import { InputNumber } from 'stwui';
	import type { NumberServiceDefinitionInput } from './shared';

	interface Props {
		input: NumberServiceDefinitionInput;
	}

	let { input = $bindable() }: Props = $props();
	let attribute = $derived(input.attribute);
</script>

<InputNumber
	bind:value={input.value}
	error={input.error}
	name="number"
	showSpin
	placeholder={attribute.datatype_description ?? undefined}
	class="relative  my-4"
>
	{#snippet label()}
		<InputNumber.Label >{attribute.description}</InputNumber.Label>
	{/snippet}
</InputNumber>
