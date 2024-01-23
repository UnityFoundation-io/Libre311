<div class="absolute left-1/2 top-5 z-[1] -translate-x-1/2">
	<div class="toggle">
		<button class="rounded-l-lg">Map</button>
		<button class="rounded-r-lg">List</button>
	</div>
</div>

<style>
	.toggle {
		display: flex;
	}

	button {
		padding: 0.5rem 2rem;
		border: 2px solid hsl(var(--secondary));
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
	}

	button:hover {
		background-color: hsl(var(--primary-hover));
	}
</style>
