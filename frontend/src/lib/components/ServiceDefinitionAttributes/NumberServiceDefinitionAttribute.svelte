<script lang="ts">
	import { InputNumber } from 'stwui';
	import type { NumberServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';

	export let attribute: NumberServiceDefinitionAttribute;
</script>

<InputNumber
	name="number"
	showSpin
	placeholder={attribute.datatype_description ?? undefined}
	class="relative mx-8 my-4"
>
	<InputNumber.Label slot="label">{attribute.description}</InputNumber.Label>
</InputNumber>
