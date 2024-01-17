<script lang='ts'>
	import messages from '$media/messages.json';
	import rocketLaunchIcon from '$lib/assets/rocket-launch.svg';
	import { fade, draw } from 'svelte/transition';
	import { Progress } from 'stwui';
	import { onMount } from 'svelte';

	let visible = false;

	const inner = `M15.59 14.37a6 6 0 0 1-5.84 7.38v-4.8m5.84-2.58a14.98 14.98 0 0 0 6.16-12.12A14.98 14.98 0 0 0 9.631 8.41m5.96 5.96a14.926 14.926 0 0 1-5.841 2.58m-.119-8.54a6 6 0 0 0-7.381 5.84h4.8m2.581-5.84a14.927 14.927 0 0 0-2.58 5.84m2.699 2.7c-.103.021-.207.041-.311.06a15.09 15.09 0 0 1-2.448-2.448 14.9 14.9 0 0 1 .06-.312m-2.24 2.39a4.493 4.493 0 0 0-1.757 4.306 4.493 4.493 0 0 0 4.306-1.758M16.5 9a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0Z`

	onMount(() => {
		visible = true;
	});
</script>

<div class='splashLoad flex flex-col justify-center items-center'>
	{#if visible}

		<div class='size-28 m-2'>
			<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1" stroke="hsl(var(--primary))" xml:space="preserve">
				<g out:fade={{ duration: 200 }} opacity="1">
					<path class="st0" in:draw={{ duration: 3000 }} stroke-linecap="round" stroke-linejoin="round" style="stroke-width: 1" d={inner} />
				</g>
			</svg>
		</div>

		<h1 class='m-2 text-2xl'>{messages['metadata']['app']}</h1>
		<p class='m-2 text-base font-normal'>{messages['environment']['loading']}</p>
		<div class='m-2 max-w-52 w-52'>
			<Progress value={0} indeterminate size='md' />
		</div>
	{/if}
</div>

<style>
	.splashLoad {
		height: 100vh;
		background-color: hsl(var(--background));
		color: hsl(var(--content));
	}

	svg {
		width: 100%;
		height: 100%;
	}

	path {
		fill: rgba(0,0,0,0);
		opacity: 1;
	}
</style>
