<script lang="ts">
	import { toTimeStamp } from '$lib/utils/functions';
	import type { DateTimeServiceDefinitionAttributeInput } from '../ServiceDefinitionAttributes/shared';

	interface Props {
		attributes: DateTimeServiceDefinitionAttributeInput;
	}

	let { attributes }: Props = $props();
</script>

<strong>{attributes.attribute.description}</strong>
<div>{toTimeStamp(attributes.value)}</div>
