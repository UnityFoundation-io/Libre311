<script lang="ts">
	import type { SingleValueListServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: SingleValueListServiceDefinitionAttributeInput;

	function extractAttributeValueNames(attributes: SingleValueListServiceDefinitionAttributeInput) {
		const keys = attributes.value;
		for (let attribute of attributes.attribute.values) {
			if (keys && keys.includes(attribute.key)) {
				return attribute.name;
			}
		}
	}

	$: value = extractAttributeValueNames(attributes);
</script>

<strong>{attributes.attribute.description}</strong>

<div>{value}</div>
