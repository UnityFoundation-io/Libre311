<script lang="ts">
	import type { DateTimeServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: DateTimeServiceDefinitionAttributeInput;

	function toTimeStamp(timeStamp: string | null | undefined) {
		return timeStamp
			? `${new Date(timeStamp).toLocaleDateString()} ${new Date(timeStamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
			: '';
	}
</script>

<strong>{attributes.attribute.description}</strong>

<div>{toTimeStamp(attributes.attribute.value.name)}</div>
