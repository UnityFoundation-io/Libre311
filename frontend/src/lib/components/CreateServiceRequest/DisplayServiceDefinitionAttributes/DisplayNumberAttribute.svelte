<script lang="ts">
	import type { NumberServiceDefinitionInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: NumberServiceDefinitionInput;
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.attribute.value.name}</div>
