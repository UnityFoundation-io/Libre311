<script lang="ts">
	import type { StringServiceDefinitionInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: StringServiceDefinitionInput;
	}

	let { attributes }: Props = $props();
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.value ?? ''}</div>
