<script lang="ts">
	import { Input } from 'stwui';

	import type { StringServiceDefinitionInput } from './shared';

	export let input: StringServiceDefinitionInput;
</script>

<Input
	bind:value={input.value}
	error={input.error}
	name="string"
	placeholder={input.attribute.datatype_description ?? undefined}
	class="relative my-4"
>
	<Input.Label slot="label">{input.attribute.description}</Input.Label>
</Input>
