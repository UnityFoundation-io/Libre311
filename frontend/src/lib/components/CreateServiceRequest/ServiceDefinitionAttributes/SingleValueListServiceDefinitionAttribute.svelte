<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type { SingleValueListServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import type { SingleValueListServiceDefinitionAttributeInput } from './shared';

	export let input: SingleValueListServiceDefinitionAttributeInput;
	$: attribute = input.attribute;

	$: selectOptions = createSelectOptions(attribute);

	function createSelectOptions(res: SingleValueListServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
</script>

<Select
	bind:value={input.value}
	error={input.error}
	name="singleselect"
	placeholder={attribute.datatype_description ?? undefined}
	options={selectOptions}
	class="relative my-4"
>
	<Select.Label slot="label">{attribute.description}</Select.Label>
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
