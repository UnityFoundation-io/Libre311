<script lang="ts">
	import { TextArea } from 'stwui';
	import type { TextServiceDefinitionAttributeInput } from './shared';

	interface Props {
		input: TextServiceDefinitionAttributeInput;
	}

	let { input = $bindable() }: Props = $props();
	let attribute = $derived(input.attribute);
</script>

<TextArea
	bind:value={input.value}
	error={input.error}
	name="text"
	placeholder={attribute.datatype_description ?? undefined}
	class="relative my-4"
>
	{#snippet label()}
		<TextArea.Label >{attribute.description}</TextArea.Label>
	{/snippet}
</TextArea>
