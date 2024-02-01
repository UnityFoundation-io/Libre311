<script lang="ts">
	import { DatePicker } from 'stwui';
	import type { DateTimeServiceDefinitionAttributeInput } from './shared';

	export let input: DateTimeServiceDefinitionAttributeInput;
	$: attribute = input.attribute;
</script>

<DatePicker
	bind:value={input.value}
	error={input.error}
	name="datetime"
	label={attribute.datatype_description ?? undefined}
	showTime
	class="relative  my-4"
>
	<DatePicker.Label slot="label">{attribute.description}</DatePicker.Label>
	<!-- TODO: Data Prop -->
	<DatePicker.Trailing slot="trailing" />
</DatePicker>
