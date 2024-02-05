<script lang="ts">
	import { toTimeStamp } from '$lib/utils/functions';
	import type { DateTimeServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	export let attributes: DateTimeServiceDefinitionAttributeInput;
</script>

<strong>{attributes.attribute.description}</strong>
<div>{toTimeStamp(attributes.value)}</div>
