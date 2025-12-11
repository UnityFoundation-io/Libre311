<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type { MultiSelectServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	import type { MultiSelectServiceDefinitionAttributeInput } from './shared';

	interface Props {
		input: MultiSelectServiceDefinitionAttributeInput;
	}

	let { input = $bindable() }: Props = $props();


	function createSelectOptions(res: MultiSelectServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
	let selectOptions = $derived(createSelectOptions(input.attribute));
</script>

<Select
	error={input.error}
	bind:value={input.value}
	name="multiselect"
	placeholder={input.attribute.datatype_description ?? undefined}
	multiple
	options={selectOptions}
	class="relative  my-4"
>
	{#snippet label()}
		<Select.Label >
			{input.attribute.description}
			{#if input.attribute.required}
				<span class="text-red-600">*</span>
			{/if}
		</Select.Label>
	{/snippet}
	<!-- @migration-task: migrate this slot by hand, `options` would shadow a prop on the parent component -->
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
