<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type { SingleValueListServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import type { SingleValueListServiceDefinitionAttributeInput } from './shared';

	interface Props {
		input: SingleValueListServiceDefinitionAttributeInput;
	}

	let { input = $bindable() }: Props = $props();


	function createSelectOptions(res: SingleValueListServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
	let attribute = $derived(input.attribute);
	let selectOptions = $derived(createSelectOptions(attribute));
</script>

<Select
	bind:value={input.value}
	error={input.error}
	name="singleselect"
	placeholder={attribute.datatype_description ?? undefined}
	options={selectOptions}
	class="relative my-4"
>
	{#snippet label()}
		<Select.Label >{attribute.description}</Select.Label>
	{/snippet}
	<!-- @migration-task: migrate this slot by hand, `options` would shadow a prop on the parent component -->
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
