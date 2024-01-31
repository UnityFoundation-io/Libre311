<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type {
		AttributeResponse,
		BaseServiceDefinitionAttribute,
		MultiSelectServiceDefinitionAttribute
	} from '$lib/services/Libre311/Libre311';

	import type { MultiSelectServiceDefinitionAttributeInput } from './shared';

	export let input: MultiSelectServiceDefinitionAttributeInput;

	$: selectOptions = createSelectOptions(input.attribute);

	function createSelectOptions(res: MultiSelectServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
</script>

<Select
	error={input.error}
	bind:value={input.value}
	name="multiselect"
	placeholder={input.attribute.datatype_description ?? undefined}
	multiple
	options={selectOptions}
	class="relative mx-8 my-4"
>
	<Select.Label slot="label">{input.attribute.description}</Select.Label>
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
