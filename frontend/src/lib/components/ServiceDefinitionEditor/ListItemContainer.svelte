<script lang="ts">
	import { List } from 'stwui';
	interface Props {
		leading?: import('svelte').Snippet;
		main?: import('svelte').Snippet;
		trailing?: import('svelte').Snippet;
	}

	let { leading, main, trailing }: Props = $props();
</script>

<List.Item class="flex cursor-pointer items-center hover:bg-slate-100">
	{@render leading?.()}
	<div class="mx-4 w-full">
		{@render main?.()}
	</div>

	<div class="flex justify-end">
		<div class="mx-2 flex items-center justify-center">
			{@render trailing?.()}
		</div>
	</div>
</List.Item>
