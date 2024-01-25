<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type { MultiSelectServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	export let attribute: MultiSelectServiceDefinitionAttribute;

	$: selectOptions = createSelectOptions(attribute);

	function createSelectOptions(res: MultiSelectServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
</script>

<Select
	name="multiselect"
	class="relative mx-8 my-4"
	on:change
	placeholder={attribute.datatype_description ?? undefined}
	multiple
	options={selectOptions}
>
	<Select.Label slot="label">{attribute.description}</Select.Label>
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
