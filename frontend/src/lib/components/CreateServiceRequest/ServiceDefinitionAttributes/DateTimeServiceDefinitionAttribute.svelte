<script lang="ts">
	import { DatePicker } from 'stwui';
	import type { DateTimeServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	export let attribute: DateTimeServiceDefinitionAttribute;
</script>

<DatePicker
	name="datetime"
	on:change
	label={attribute.datatype_description ?? undefined}
	showTime
	class="relative mx-8 my-4"
>
	<DatePicker.Label slot="label">{attribute.description}</DatePicker.Label>
	<!-- TODO: Data Prop -->
	<DatePicker.Trailing slot="trailing" />
</DatePicker>
