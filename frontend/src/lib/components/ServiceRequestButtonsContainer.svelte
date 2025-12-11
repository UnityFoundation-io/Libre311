<script lang="ts">
	interface Props {
		left?: import('svelte').Snippet;
		right?: import('svelte').Snippet;
	}

	let { left, right }: Props = $props();
</script>

<div class="flex items-center justify-between pb-4 pt-4">
	{@render left?.()}
	{@render right?.()}
</div>
