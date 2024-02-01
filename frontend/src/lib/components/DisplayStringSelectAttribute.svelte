<script lang="ts">
	import type { StringServiceDefinitionInput } from './CreateServiceRequest/ServiceDefinitionAttributes/shared';

	export let attributes: StringServiceDefinitionInput;
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.attribute.value.name}</div>
