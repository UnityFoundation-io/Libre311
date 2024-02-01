<script lang="ts">
	import type { SingleValueListServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: SingleValueListServiceDefinitionAttributeInput;

	function extractValues(attributes: SingleValueListServiceDefinitionAttributeInput) {
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
	{#each values as value}
		<li>
			{value}
		</li>
	{/each}
</ul>
