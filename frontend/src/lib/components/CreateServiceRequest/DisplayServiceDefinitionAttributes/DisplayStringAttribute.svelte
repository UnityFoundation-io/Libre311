<script lang="ts">
	import type { StringServiceDefinitionInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: StringServiceDefinitionInput;
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.value}</div>
