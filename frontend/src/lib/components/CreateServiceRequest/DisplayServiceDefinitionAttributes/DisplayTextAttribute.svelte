<script lang="ts">
	import type { TextServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: TextServiceDefinitionAttributeInput;
	}

	let { attributes }: Props = $props();
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.value ?? ''}</div>
