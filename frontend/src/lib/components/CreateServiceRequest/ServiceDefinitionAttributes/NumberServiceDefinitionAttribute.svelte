<script lang="ts">
	import { InputNumber } from 'stwui';
	import type { NumberServiceDefinitionInput } from './shared';

	export let input: NumberServiceDefinitionInput;
	$: attribute = input.attribute;
</script>

<InputNumber
	bind:value={input.value}
	error={input.error}
	name="number"
	showSpin
	placeholder={attribute.datatype_description ?? undefined}
	class="relative  my-4"
>
	<InputNumber.Label slot="label">
		{attribute.description}
		{#if attribute.required}
			<span class="text-red-600">*</span>
		{/if}
	</InputNumber.Label>
</InputNumber>
