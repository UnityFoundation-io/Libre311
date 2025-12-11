<script lang="ts">
	interface Props {
		startingValue: boolean;
		children?: import('svelte').Snippet<[any]>;
	}

	let { startingValue = $bindable(), children }: Props = $props();

	function toggle() {
		startingValue = !startingValue;
	}
</script>

{@render children?.({ show: startingValue, toggle, })}
