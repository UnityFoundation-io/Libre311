<script lang="ts">
	import type { MultiSelectServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: MultiSelectServiceDefinitionAttributeInput;

	function extractAttributeValueNames(attributes: MultiSelectServiceDefinitionAttributeInput) {
		let values: string[] = [];

		const keys = attributes.value;
		if (attributes.attribute.values) {
			for (let attribute of attributes.attribute.values) {
				if (!attribute.key || !attribute.name)
					return [];
				if (keys && keys.includes(attribute.key)) {
					values.push(attribute.name);
				}
			}

			return values;
		} else {
			return [];
		}
	}

	$: values = extractAttributeValueNames(attributes);
</script>

<strong>{attributes.attribute.description}</strong>
<div>{values.join(', ')}</div>
