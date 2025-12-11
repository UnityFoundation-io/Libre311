<script lang="ts">
	import { Input } from 'stwui';

	import type { StringServiceDefinitionInput } from './shared';

	interface Props {
		input: StringServiceDefinitionInput;
	}

	let { input = $bindable() }: Props = $props();
</script>

<Input
	bind:value={input.value}
	error={input.error}
	name="string"
	placeholder={input.attribute.datatype_description ?? undefined}
	class="relative my-4"
>
	{#snippet label()}
		<Input.Label >
			{input.attribute.description}
			{#if input.attribute.required}
				<span class="text-red-600">*</span>
			{/if}
		</Input.Label>
	{/snippet}
</Input>
