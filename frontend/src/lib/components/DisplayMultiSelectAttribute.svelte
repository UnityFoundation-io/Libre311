<script lang="ts">
	import type {
		MultiSelectServiceDefinitionAttributeInput,
		SingleValueListServiceDefinitionAttributeInput
	} from './CreateServiceRequest/ServiceDefinitionAttributes/shared';

	export let attributes:
		| MultiSelectServiceDefinitionAttributeInput
		| SingleValueListServiceDefinitionAttributeInput;
</script>

<strong>{attributes.attribute.description}</strong>

<ul>
	{#each attributes.attribute.values as value, i}
		{value.name}{#if i < attributes.attribute.values.length - 1}<span>{', '}</span>{/if}
	{/each}
</ul>
