<script lang="ts">
	import type { MultiSelectServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: MultiSelectServiceDefinitionAttributeInput;
	}

	let { attributes }: Props = $props();

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

	let values = $derived(extractAttributeValueNames(attributes));
</script>

<strong>{attributes.attribute.description}</strong>
<div>{values.join(', ')}</div>
