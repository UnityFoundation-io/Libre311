<script lang="ts">
	import type { NumberServiceDefinitionInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: NumberServiceDefinitionInput;
	}

	let { attributes }: Props = $props();
</script>

<strong>{attributes.attribute.description}</strong>
<div>{attributes.value}</div>
