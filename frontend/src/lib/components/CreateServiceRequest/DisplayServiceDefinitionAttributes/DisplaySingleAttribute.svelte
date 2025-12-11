<script lang="ts">
	import type { SingleValueListServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: SingleValueListServiceDefinitionAttributeInput;
	}

	let { attributes }: Props = $props();

	function extractAttributeValueNames(attributes: SingleValueListServiceDefinitionAttributeInput) {
		const keys = attributes.value;
		for (let attribute of attributes.attribute.values) {
			if (keys && keys.includes(attribute.key)) {
				return attribute.name;
			}
		}
	}

	let value = $derived(extractAttributeValueNames(attributes));
</script>

<strong>{attributes.attribute.description}</strong>

<div>{value}</div>
