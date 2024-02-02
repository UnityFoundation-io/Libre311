<script lang="ts">
	import type { DateTimeServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: DateTimeServiceDefinitionAttributeInput;

	function toTimeStamp(timeStamp: Date | null | undefined) {
		return timeStamp
			? `${timeStamp.toLocaleDateString()} ${timeStamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
			: '';
	}
</script>

<strong>{attributes.attribute.description}</strong>
<div>{toTimeStamp(attributes.value)}</div>
