<script module lang="ts">
	export const xMark = `<svg xmlns="http://www.w3.org/2000/svg" fill="red" viewBox="0 0 24 24" stroke-width="1.5" stroke="red" class="w-6 h-6"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" /></svg>`;
</script>

<script lang="ts">
	import type { TwSizes } from 'stwui/types';
	interface Props {
		size?: TwSizes;
	}

	let { size = '6' }: Props = $props();
</script>

<svg
	xmlns="http://www.w3.org/2000/svg"
	fill="red"
	viewBox="0 0 24 24"
	stroke-width="1.5"
	stroke="red"
	class="w-{size} h-{size}"
>
	<path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
</svg>
