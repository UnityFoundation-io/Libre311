<script lang="ts">
	import type { SelectedValue } from '$lib/services/Libre311/Libre311';

	export let selectedValues: SelectedValue[] = [];
</script>

{#each selectedValues as selectedValue}
	<strong class="text-base">{selectedValue.description}</strong>
	<p class="text-sm">{selectedValue.values.map((val) => val.name).join(', ')}</p>
{/each}
