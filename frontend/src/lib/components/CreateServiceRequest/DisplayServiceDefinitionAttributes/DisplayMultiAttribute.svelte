<script lang="ts">
	import type { MultiSelectServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: MultiSelectServiceDefinitionAttributeInput;

	function extractValues(attributes: MultiSelectServiceDefinitionAttributeInput) {
		let values: string[] = [];

		for (let attribute of attributes.attribute.values) {
			if (keys && keys.includes(attribute.key)) {
				values.push(attribute.name);
			}
		}

		return values;
	}

	$: keys = attributes.value;
	$: values = extractValues(attributes);
</script>

<strong>{attributes.attribute.description}</strong>

<ul>
	{#each values as value, i}
		{value}{#if i < values - 1}<span>{', '}</span>{/if}
	{/each}
</ul>
