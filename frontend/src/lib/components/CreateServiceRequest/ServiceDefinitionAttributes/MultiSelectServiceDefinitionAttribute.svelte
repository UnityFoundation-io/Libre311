<script lang="ts" context="module">
	type AttributeSelection = {
		code: BaseServiceDefinitionAttribute['code'];
		attributeResponse: AttributeResponse[] | AttributeResponse;
	};
</script>

<script lang="ts">
	import { Select } from 'stwui';
	import type { SelectOption } from 'stwui/types';
	import type {
		AttributeResponse,
		BaseServiceDefinitionAttribute,
		MultiSelectServiceDefinitionAttribute
	} from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher } from 'svelte';

	export let attribute: MultiSelectServiceDefinitionAttribute;

	const dispatch = createEventDispatcher<{ change: AttributeSelection }>();

	let selectOptionsKeyArray: string[] = [];

	$: selectOptions = createSelectOptions(attribute);

	$: dispatch('change', {
		code: attribute.code,
		attributeResponse: toAttributeResponse(selectOptionsKeyArray)
	});

	function toAttributeResponse(selectedKeys: string[]): AttributeResponse[] {
		return selectedKeys.map((key) => ({ code: attribute.code, value: key }));
	}

	function createSelectOptions(res: MultiSelectServiceDefinitionAttribute): SelectOption[] {
		return res.values.map((s) => ({ value: s.key, label: s.name }));
	}
</script>

<Select
	bind:value={selectOptionsKeyArray}
	name="multiselect"
	placeholder={attribute.datatype_description ?? undefined}
	multiple
	options={selectOptions}
	class="relative mx-8 my-4"
>
	<Select.Label slot="label">{attribute.description}</Select.Label>
	<Select.Options slot="options">
		{#each selectOptions as option}
			<Select.Options.Option {option} />
		{/each}
	</Select.Options>
</Select>
