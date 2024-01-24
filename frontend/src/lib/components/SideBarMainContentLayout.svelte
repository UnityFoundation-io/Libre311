<script lang="ts">
	export let sideBarBreakpointActive = true;
</script>

<div class="layout-container">
	<aside class="side-bar" class:collapsable-side-bar={sideBarBreakpointActive}>
		<slot name="side-bar" />
	</aside>
	<div class="main-content"><slot name="main-content" /></div>
</div>

<style>
	.layout-container {
		display: flex;
		grid-template-columns: 400px auto;
		height: 100%;
	}
	.main-content {
		flex-grow: 1;
		max-height: calc(100vh - var(--header-height));
	}

	.collapsable-side-bar {
		display: none;
	}

	.side-bar {
		width: 400px;
	}

	aside {
		overflow-y: auto;
		max-height: calc(100vh - var(--header-height));
	}

	@media (min-width: 64rem) {
		.collapsable-side-bar {
			display: unset;
		}
	}
</style>
