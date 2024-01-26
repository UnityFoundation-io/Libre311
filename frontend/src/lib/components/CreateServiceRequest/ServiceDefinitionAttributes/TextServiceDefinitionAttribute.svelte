<script lang="ts">
	import { TextArea } from 'stwui';
	import type { TextServiceDefinitionAttribute } from '$lib/services/Libre311/Libre311';
	import { readonly } from 'svelte/store';

	export let attribute: TextServiceDefinitionAttribute;
</script>

<TextArea
	name="text"
	placeholder={attribute.datatype_description ?? undefined}
	readonly
	class="relative mx-8 my-4"
>
	<TextArea.Label slot="label">{attribute.description}</TextArea.Label>
</TextArea>
