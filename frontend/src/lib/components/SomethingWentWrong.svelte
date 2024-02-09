<script>
	import { fade } from 'svelte/transition';
</script>

<div
	class="flex h-full w-full items-center justify-center"
	style="background-color: hsl(var(--primary));height: 100dvh;"
>
	<div>
		<h1 in:fade={{ duration: 2000 }}>
			{`:( Oh No!`}
		</h1>
		<h1 in:fade={{ delay: 1000, duration: 2000 }}>
			{`Something Went Wrong`}
		</h1>
	</div>
</div>

<style>
	h1 {
		font-size: 4.5rem;
		color: hsl(var(--primary-content));
		margin: 2%;
	}
</style>
