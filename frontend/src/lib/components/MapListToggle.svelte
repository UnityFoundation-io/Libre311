<script>
	import { page } from '$app/stores';
</script>

<div class="toggle">
	<a href="/issues/map">
		<button class="rounded-l-lg" class:selected={$page.route.id == '/issues/map'}> Map </button>
	</a>

	<a href="/issues/list">
		<button class="rounded-r-lg" class:selected={$page.route.id == '/issues/list'}>List </button>
	</a>
</div>

<style>
	.toggle {
		display: flex;
	}

	button {
		padding: 0.5rem 2rem;
		background-color: hsl(var(--surface));
		color: hsl(var(--content));
		border: 1px solid hsl(var(--primary));
		position: relative;
	}

	button:hover::before {
		--tw-surface-opacity: 0.05;
		top: 0;
		bottom: 0;
		left: 0;
		right: 0;
		content: '';
		position: absolute;
		opacity: var(--tw-surface-opacity);
		background-color: hsl(var(--hover));
	}

	.selected {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
	}
</style>
