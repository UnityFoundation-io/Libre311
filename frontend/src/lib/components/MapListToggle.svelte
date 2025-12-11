<script>
	import { page } from '$app/state';
	import { useLibre311Context } from '$lib/context/Libre311Context';

	const linkResolver = useLibre311Context().linkResolver;
</script>

<div class="toggle">
	<a href={linkResolver.issuesMap(page.url)}>
		<button class="rounded-l-lg" class:selected={page.route.id == '/issues/map'}> Map </button>
	</a>

	<a href={linkResolver.issuesList(page.url)}>
		<button class="rounded-r-lg" class:selected={page.route.id == '/issues/list'}>List </button>
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
