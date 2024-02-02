<script lang="ts">
	import type { TextServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: TextServiceDefinitionAttributeInput;
</script>

<strong>{attributes.attribute.description}</strong>

<div>{attributes.value ?? ''}</div>
