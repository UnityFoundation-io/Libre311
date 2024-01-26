<script lang="ts">
	import { Input } from 'stwui';
	import type { StringServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	export let attribute: StringServiceDefinitionAttribute;
</script>

<Input
	name="string"
	on:change
	placeholder={attribute.datatype_description ?? undefined}
	class="relative mx-8 my-4"
>
	<Input.Label slot="label">{attribute.description}</Input.Label>
</Input>
