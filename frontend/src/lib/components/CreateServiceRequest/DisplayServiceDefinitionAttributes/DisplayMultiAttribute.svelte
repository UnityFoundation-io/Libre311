<script lang="ts">
	import type { MultiSelectServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: MultiSelectServiceDefinitionAttributeInput;

	function extractAttributeValueNames(attributes: MultiSelectServiceDefinitionAttributeInput) {
		let values: string[] = [];

		const keys = attributes.value;
		for (let attribute of attributes.attribute.values) {
			if (keys && keys.includes(attribute.key)) {
				values.push(attribute.name);
			}
		}
		return values;
	}

	$: values = extractAttributeValueNames(attributes);
</script>

<strong>{attributes.attribute.description}</strong>
<div>{values.join(', ')}</div>
