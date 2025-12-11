<script lang="ts">
	import messages from '$media/messages.json';
	import { rocketLaunch } from '$lib/components/Svg/outline/rocket-launch.js';
	import { fade, draw } from 'svelte/transition';
	import { onMount } from 'svelte';
    import {Progress} from "$lib/components/ui/progress";

	let visible = $state(false);

	onMount(() => {
		visible = true;
	});
</script>

<div class="splashLoad flex flex-col items-center justify-center">
	{#if visible}
		<div class="m-2 size-28">
			<svg
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
				stroke-width="1"
				stroke="hsl(var(--primary))"
				xml:space="preserve"
			>
				<g out:fade={{ duration: 200 }} opacity="1">
					<path
						class="st0"
						in:draw={{ duration: 3000 }}
						stroke-linecap="round"
						stroke-linejoin="round"
						style="stroke-width: 1"
						d={rocketLaunch}
					/>
				</g>
			</svg>
		</div>
	{/if}

	<h1 class="m-2 text-2xl">{messages['metadata']['app']}</h1>
	<p class="m-2 text-base font-normal">{messages['environment']['loading']}</p>
	<div class="m-2 w-52 max-w-52">
		<Progress value={0}  />
	</div>
</div>

<style>
	.splashLoad {
		height: 100dvh;
		background-color: hsl(var(--background));
		color: hsl(var(--content));
	}

	svg {
		width: 100%;
		height: 100%;
	}

	path {
		fill: rgba(0, 0, 0, 0);
		opacity: 1;
	}
</style>
