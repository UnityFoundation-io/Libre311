<script lang="ts">
	import { DatePicker } from 'stwui';
	import type { DateTimeServiceDefinitionAttributeInput } from './shared';

	interface Props {
		input: DateTimeServiceDefinitionAttributeInput;
	}

	let { input = $bindable() }: Props = $props();
	let attribute = $derived(input.attribute);
</script>

<DatePicker
	bind:value={input.value}
	error={input.error}
	name="datetime"
	label={attribute.datatype_description ?? undefined}
	showTime
	class="relative  my-4"
>
	<!-- @migration-task: migrate this slot by hand, `label` would shadow a prop on the parent component -->
	<DatePicker.Label slot="label">{attribute.description}</DatePicker.Label>
	<!-- TODO: Data Prop -->
	{#snippet trailing()}
		<DatePicker.Trailing  />
	{/snippet}
</DatePicker>
