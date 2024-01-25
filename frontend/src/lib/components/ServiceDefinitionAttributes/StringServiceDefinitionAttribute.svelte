<script lang="ts">
	import { Input } from 'stwui';
	import type { StringServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	export let attribute: StringServiceDefinitionAttribute;
</script>

<Input name="string" placeholder={attribute.datatype_description ?? undefined}>
	<Input.Label slot="label">{attribute.description}</Input.Label>
</Input>
