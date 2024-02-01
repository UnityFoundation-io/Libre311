<script lang="ts">
	import type {
		StringServiceDefinitionInput,
		NumberServiceDefinitionInput
	} from './CreateServiceRequest/ServiceDefinitionAttributes/shared';

	export let attributes: StringServiceDefinitionInput | NumberServiceDefinitionInput;
</script>

<strong>{attributes.attribute.description}</strong>

{attributes.attribute.value.name}
