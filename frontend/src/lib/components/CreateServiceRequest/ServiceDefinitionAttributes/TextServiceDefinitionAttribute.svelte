<script lang="ts">
	import { TextArea } from 'stwui';
	import type { TextServiceDefinitionAttributeInput } from './shared';

	export let input: TextServiceDefinitionAttributeInput;
	$: attribute = input.attribute;
</script>

<TextArea
	bind:value={input.value}
	error={input.error}
	name="text"
	placeholder={attribute.datatype_description ?? undefined}
	class="relative my-4"
>
	<TextArea.Label slot="label">{attribute.description}</TextArea.Label>
</TextArea>
